package com.und.service


import com.sun.org.apache.xpath.internal.operations.Bool
import com.und.config.StreamClass
import com.und.exception.EventNotFoundException
import com.und.model.*
import com.und.model.DataType
import com.und.model.Event
import com.und.model.Unit
import com.und.model.mongo.*
import com.und.model.web.EventMessage
import com.und.repository.mongo.IpLocationRepository
import com.und.repository.mongo.MetadataRepository
import com.und.service.segmentquerybuilder.SegmentService
import com.und.model.web.Event as WebEvent
import com.und.model.mongo.Event as MongoEvent
import com.und.utils.Constants
import com.und.utils.DateUtils
import com.und.utils.MongoEventUtils
import com.und.utils.parseUserAgentString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

/**
 *  make sure before computing segment event should be saved first. use multiple consumer for save.
 */
@Service
class EventSegmentProcessing {

    @Autowired
    private lateinit var dateUtils: DateUtils

    @Autowired
    private lateinit var ipLocationRepository: IpLocationRepository

    @Autowired
    private lateinit var metadataRepository: MetadataRepository

    @Autowired
    private lateinit var mongoEventUtils: MongoEventUtils

    @Autowired
    private lateinit var streamClass: StreamClass

    @Autowired
    private lateinit var segmentService: SegmentService

    @StreamListener(Constants.PROCESS_SEGMENT)
    fun processSegment(event: WebEvent) {
        val mongoEvent = buildMongoEvent(event)
        val liveSegments = getMetadataOfLiveSegment(event.clientId, "live", false)
        liveSegments.forEach {
            checkEventEffectOnSegment(mongoEvent, Metadata())
        }
        sendForLiveProcessing(mongoEvent)
        val pastSegments = getMetadataOfLiveSegment(event.clientId, "past", false)
        pastSegments.forEach {
            checkEventEffectOnSegment(mongoEvent, Metadata())
        }
    }

    private fun getMetadataOfLiveSegment(clientId: Long, status: String, stopped: Boolean): List<Metadata> {
        return metadataRepository.findByClientIdAndTypeAndStopped(clientId, status, stopped)
    }

    private fun buildMongoEvent(event: com.und.model.web.Event): com.und.model.mongo.Event {
        var mongoEvent = MongoEvent(clientId = event.clientId, name = event.name)
        mongoEvent.timeZoneId = ZoneId.of(event.timeZone)
        mongoEvent.creationTime = Date.from(Instant.ofEpochMilli(event.creationTime).atZone(ZoneId.of("UTC")).toInstant())
        mongoEvent = mongoEvent.parseUserAgentString(event.agentString)
        if (event.country != null && event.state != null && event.city != null) {
            mongoEvent.geogrophy = Geogrophy(event.country, event.state, event.city)
        } else {
            event.ipAddress?.let {
                mongoEvent.geogrophy = ipLocationRepository.getGeographyByIpAddress(it)
            }
        }
        //FIXME hard coded charged
        if ("charged".equals(event.name, ignoreCase = false)) {
            mongoEvent.lineItem = event.lineItem
            mongoEvent.lineItem.forEach { item ->
                item.properties = mongoEventUtils.toDateInMap(item.properties)

            }
        }
        //copy attributes
        mongoEvent.attributes.putAll(mongoEventUtils.toDateInMap(event.attributes))
        if (event.identity.idf == 1) {
            mongoEvent.userIdentified = true
        }
        return mongoEvent
    }

    fun sendForLiveProcessing(mongoEvent: com.und.model.mongo.Event) {
        streamClass.outEventForLiveProcessing().send(MessageBuilder.withPayload(buildEventForLiveSegment(mongoEvent)).build())
    }

    fun buildEventForLiveSegment(fromEvent: com.und.model.mongo.Event): EventMessage {
        return fromEvent.id?.let {
            EventMessage(it, fromEvent.clientId, fromEvent.userId, fromEvent.name, fromEvent.creationTime, fromEvent.userIdentified)
        } ?: throw EventNotFoundException("Event with null id")
    }

    fun checkEventEffectOnSegment(event: MongoEvent, metadata: Metadata) {
        val clientId = metadata.clientId!!
        val segmentId = metadata.id!!
        val userId = event.userId!!
        when (metadata.criteriaGroup) {
            SegmentCriteriaGroup.DID -> {
                //if return true then add userID in list no need computation.
                val isEventMatch = checkEvent(metadata.didEvents, event)
                if (isEventMatch) {
                    when (metadata.conditionalOperator) {
                        ConditionType.AllOf -> {
                            if (metadata.didEventsSize > 1) {
                                //compute
                                val isPresent = segmentService.isUserPresentInSegment(metadata.segment, userId = event.userId!!, includeUsers = IncludeUsers.ALL, clientId = metadata.clientId!!, campaign = null)
                                if (isPresent) {
                                    segmentService.addUserInSegment(clientId = clientId, userId = userId, segmentId = segmentId)
                                } else {
                                    //Do nothing
                                }
                            } else {
                                //add directly if not present
                                segmentService.addUserInSegment(clientId = clientId, userId = userId, segmentId = segmentId)
                            }
                        }
                        ConditionType.AnyOf -> {
                            //add directly if not present
                            segmentService.addUserInSegment(clientId = clientId, userId = userId, segmentId = segmentId)
                        }
                    }
                } else {
                    //Do nothing this event have no effect on segment
                }

            }
            SegmentCriteriaGroup.DIDNOT -> {
                //if return true then remove userID in list no need computation.
                val isEventMatch = checkEvent(metadata.didNotEvents, event)
                if (isEventMatch) {
                    //removing userid
                    segmentService.removeUserFromSegment(userId, clientId, segmentId)
                } else {
                    //adding userid  do nothing because in did not case we have possibility to remove only not add but we have to compute segemnt during new user creation
                    segmentService.addUserInSegment(userId, clientId, segmentId)
                }

            }
            SegmentCriteriaGroup.DID_DIDNOT -> {
                val isDidEventMatch = checkEvent(metadata.didEvents, event)
                if (isDidEventMatch) {
                    //compute
                    val isPresent = segmentService.isUserPresentInSegment(metadata.segment, userId = event.userId!!, includeUsers = IncludeUsers.ALL, clientId = metadata.clientId!!, campaign = null)
                    if (isPresent) {
                        segmentService.addUserInSegment(clientId = clientId, userId = userId, segmentId = segmentId)
                    } else {
                        //Do nothing
                    }
                } else {
                    val isDidNotEventMatch = checkEvent(metadata.didNotEvents, event)
                    if (isDidNotEventMatch) {
                        //remove user
                        segmentService.removeUserFromSegment(userId, clientId, segmentId)
                    } else {
                        //compute
                        val isPresent = segmentService.isUserPresentInSegment(metadata.segment, userId = event.userId!!, includeUsers = IncludeUsers.ALL, clientId = metadata.clientId!!, campaign = null)
                        if (isPresent) {
                            segmentService.addUserInSegment(clientId = clientId, userId = userId, segmentId = segmentId)
                        } else {
                            //Do nothing
                        }
                    }
                }
            }
            SegmentCriteriaGroup.DID_USERPROP -> {
                // if pass did then check userprop only if pass
                val didMatch = checkEvent(metadata.didEvents, event)
                if(didMatch){
                    val matchUserProp = segmentService.isUserPresentInSegmentWithUserPropOnly(metadata.segment,clientId,IncludeUsers.ALL,null,event.userId!!)
                    if(matchUserProp){
                        segmentService.addUserInSegment(clientId = clientId, userId = userId, segmentId = segmentId)
                    }else{
                        //Do  nothing
                    }
                }else{
                    //Do nothing
                }
            }
            SegmentCriteriaGroup.DID_EVENTPROP -> {
                val isEventPropMatch = checkEventUserProperties(metadata.eventGlobalFilter, event)
                //TODO check is still did events are considerable if not do nothing
                //update segment metadata when any event become dead.
                if (isEventPropMatch) {

                    val result = metadata.didEvents.map {
                        it.consider
                    }
                    if (result.contains(true)) {
                        val isEventMatch = checkEvent(metadata.didEvents, event)
                        if (isEventMatch) {
                            when (metadata.conditionalOperator) {
                                ConditionType.AllOf -> {
                                    if (metadata.didEventsSize > 1) {
                                        //compute
                                        val isPresent = segmentService.isUserPresentInSegment(metadata.segment, userId = event.userId!!, includeUsers = IncludeUsers.ALL, clientId = metadata.clientId!!, campaign = null)
                                        if (isPresent) {
                                            segmentService.addUserInSegment(clientId = clientId, userId = userId, segmentId = segmentId)
                                        } else {
                                            //Do nothing
                                        }
                                    } else {
                                        //add directly if not present
                                        segmentService.addUserInSegment(clientId = clientId, userId = userId, segmentId = segmentId)
                                    }
                                }
                                ConditionType.AnyOf -> {
                                    //add directly if not present
                                    segmentService.addUserInSegment(clientId = clientId, userId = userId, segmentId = segmentId)
                                }
                            }
                        } else {
                            //do nothing
                        }
                    }
                } else {
                    //do nothing
                }
            }
            SegmentCriteriaGroup.DID_DIDNOT_USERPROP -> {
                //same as did and did not
                val isDidEventMatch = checkEvent(metadata.didEvents, event)
                if (isDidEventMatch) {
                    //compute
                    val isPresent = segmentService.isUserPresentInSegment(metadata.segment, userId = event.userId!!, includeUsers = IncludeUsers.ALL, clientId = metadata.clientId!!, campaign = null)
                    if (isPresent) {
                        segmentService.addUserInSegment(clientId = clientId, userId = userId, segmentId = segmentId)
                    } else {
                        //Do nothing
                    }
                } else {
                    val isDidNotEventMatch = checkEvent(metadata.didNotEvents, event)
                    if (isDidNotEventMatch) {
                        //remove user
                        segmentService.removeUserFromSegment(userId, clientId, segmentId)
                    } else {
                        //compute
                        val isPresent = segmentService.isUserPresentInSegment(metadata.segment, userId = event.userId!!, includeUsers = IncludeUsers.ALL, clientId = metadata.clientId!!, campaign = null)
                        if (isPresent) {
                            segmentService.addUserInSegment(clientId = clientId, userId = userId, segmentId = segmentId)
                        } else {
                            //Do nothing
                        }
                    }
                }
            }
            SegmentCriteriaGroup.DID_DIDNOT_EVENTPROP_USERPROP -> {

            }
            SegmentCriteriaGroup.DIDNOT_USERPROP -> {
                //if return true then remove userID in list no need computation.
                val isEventMatch = checkEvent(metadata.didNotEvents, event)
                if (isEventMatch) {
                    //removing userid
                    segmentService.removeUserFromSegment(userId, clientId, segmentId)
                } else {
                    //adding userid check user prop
                    val present  = segmentService.isUserPresentInSegmentWithUserPropOnly(metadata.segment,clientId,IncludeUsers.ALL,null,event.userId!!)
                    if(present) segmentService.addUserInSegment(userId, clientId, segmentId)
                }
            }
            SegmentCriteriaGroup.DIDNOT_EVENTPROP -> {
                val isEventPropMatch = checkEventUserProperties(metadata.eventGlobalFilter, event)
                if (isEventPropMatch) {
                    val result = metadata.didNotEvents.map {
                        it.consider
                    }
                    if (result.contains(true)) {
                        val isEventMatch = checkEvent(metadata.didNotEvents, event)
                        if (isEventMatch) {
                            //remove this userid
                            segmentService.removeUserFromSegment(userId, clientId, segmentId)
                        } else {
                            // is it needed to handle size scenario here
                            if (metadata.didNotEventSize > 1) {
                                val isPresent = segmentService.isUserPresentInSegment(metadata.segment, userId = event.userId!!, includeUsers = IncludeUsers.ALL, clientId = metadata.clientId!!, campaign = null)
                                if (isPresent) {
                                    segmentService.addUserInSegment(clientId = clientId, userId = userId, segmentId = segmentId)
                                } else {
                                    //do nothing
                                }
                            } else {
                                segmentService.addUserInSegment(userId, clientId, segmentId)
                            }
                        }
                    }
                } else {
                    //do nothing
                }
            }
            SegmentCriteriaGroup.DID_DIDNOT_EVENTPROP -> {
                val isEventPropMatch = checkEventUserProperties(metadata.eventGlobalFilter, event)

                if (isEventPropMatch) {
                    val result1 = metadata.didEvents.map {
                        it.consider
                    }
                    val result2 = metadata.didNotEvents.map {
                        it.consider
                    }
                    if (result1.contains(true) || result2.contains(true)) {
                        val isEventMatch = checkEvent(metadata.didEvents, event)
                        if (isEventMatch) {
                            when (metadata.conditionalOperator) {
                                ConditionType.AllOf -> {
                                    if (metadata.didEventsSize > 1) {
                                        //compute
                                        val isPresent = segmentService.isUserPresentInSegment(metadata.segment, userId = event.userId!!, includeUsers = IncludeUsers.ALL, clientId = metadata.clientId!!, campaign = null)
                                        if (isPresent) {
                                            segmentService.addUserInSegment(userId, clientId, segmentId)
                                        } else {
                                            segmentService.removeUserFromSegment(userId, clientId, segmentId)
                                        }
                                    } else {
                                        val isDidNotMatch = checkEvent(metadata.didNotEvents, event)
                                        if (isDidNotMatch) {
                                            //remove user
                                            segmentService.removeUserFromSegment(userId, clientId, segmentId)
                                        } else {
                                            //chek is this user full fill did not criteria if yes then add else not(remove)
                                            segmentService.addUserInSegment(userId, clientId, segmentId)
                                            //or
                                            val isPresent = segmentService.isUserPresentInSegment(metadata.segment, userId = event.userId!!, includeUsers = IncludeUsers.ALL, clientId = metadata.clientId!!, campaign = null)
                                            if (isPresent) {
                                                segmentService.addUserInSegment(userId, clientId, segmentId)
                                            }
                                        }
                                    }
                                }
                                ConditionType.AnyOf -> {
                                    val isDidNotMatch = checkEvent(metadata.didNotEvents, event)
                                    if (isDidNotMatch) {
                                        //remove user
                                        segmentService.removeUserFromSegment(userId, clientId, segmentId)
                                    } else {
                                        //Add user
                                        val isPresent = segmentService.isUserPresentInSegment(metadata.segment, userId = event.userId!!, includeUsers = IncludeUsers.ALL, clientId = metadata.clientId!!, campaign = null)
                                        if (isPresent) {
                                            segmentService.addUserInSegment(userId, clientId, segmentId)
                                        }
                                    }
                                }
                            }
                        } else {
                            //handle did not case
                        }
                    }
                } else {
                    //do nothing
                }
            }
            SegmentCriteriaGroup.EVENTPROP -> {
                //it return true then add userId
                val isEventPropMatch = checkEventUserProperties(metadata.eventGlobalFilter, event)
                val isGeogaphyMatch = checkGeographyFilter(metadata,event)
                if (isEventPropMatch && isGeogaphyMatch) segmentService.addUserInSegment(userId, clientId, segmentId)
            }
            SegmentCriteriaGroup.USERPROP -> {
                //check it on push profile do nothing
            }
            SegmentCriteriaGroup.NONE -> {

            }
        }
    }

    fun checkEvent(metadata: List<MetaEvent>, event: MongoEvent): Boolean {
        val tz = ZoneId.of("UTC")
        val filterResult = mutableListOf<Boolean>()
        val metaEvents = metadata.map { metaEvent ->
            if (metaEvent.consider) {
                if (metaEvent.name == event.name) {
                    val result: Boolean = when (DateOperator.valueOf(metaEvent.operator)) {
                        DateOperator.Between -> {
                            val dates = metaEvent.date
                            val endDate = dateUtils.getMidnight(dates.last(), tz)
                            val eventCreationTime = event.creationTime
                            when {
                                eventCreationTime.compareTo(endDate) == 0 -> true
                                else -> {
                                    //update metadata
                                    metaEvent.consider = false
                                    false
                                }
                            }
                        }
                        DateOperator.On -> {
                            val dates = metaEvent.date
                            val start = dateUtils.getStartOfDay(dates.first(), tz)
                            val end = dateUtils.getMidnight(dates.first(), tz)
                            val eventCreationTime = event.creationTime
                            when {
                                (eventCreationTime.compareTo(start) > 0 && eventCreationTime.compareTo(end) < 0) -> true
                                else -> {
                                    //update metadata
                                    metaEvent.consider = false
                                    false
                                }
                            }
                        }
                        else -> true
                    }
                    if (result) {
                        //checking event property filter
                        val eventPropMatch = checkEventAttributes(metaEvent.property, event.attributes)
                        if (eventPropMatch) filterResult.add(true) else filterResult.add(false)
                    } else {
                        filterResult.add(false)
                    }
                } else {
                    filterResult.add(false)
                }
            }
            metaEvent
        }

        return filterResult.contains(true)
    }

    fun checkEventAttributes(metaAttibutes: List<PropertyFilter>, eventAttributes: HashMap<String, Any>): Boolean {
        val groupByNameAttr = metaAttibutes.groupBy { it.name }
        val filterResult = mutableListOf<Boolean>()
        groupByNameAttr.forEach { key, propertyFilters ->
            val result = propertyFilters.map { filter ->
                if (eventAttributes.containsKey(filter.name)) {
                    when (filter.type) {
                        DataType.number -> {
                            matchNumberOperator(filter.operator, filter.values, eventAttributes[filter.name]!!)
                        }
                        DataType.boolean -> {
                            matchBooleanOperator(filter.operator, filter.values, eventAttributes[filter.name]!!)
                        }
                        DataType.date -> {
                            matchDateOperator(filter.operator, filter.values, eventAttributes[filter.name]!!)
                        }
//                        DataType.range -> {
//                            matchRangeOperator(filter.operator, filter.values, eventAttributes[filter.name]!!)
//                        }
                        DataType.string -> {
                            matchStringOperator(eventAttributes[filter.name]!! as String, filter.operator, filter.values)
                        }
                        else -> false
                    }
                } else {
                    false
                }

            }
            //if result contain single true then mark as true
            if (result.contains(true)) filterResult.add(true) else filterResult.add(false)
        }
        //if we get all true then trued
        return !filterResult.contains(false)
    }

    fun checkUserProperties(filter: Map<String, List<GlobalFilter>>, eventUser: EventUser) {
        filter.forEach { key, value ->

        }
    }

    fun checkEventUserProperties(filters: Map<String, List<GlobalFilter>>, event: MongoEvent): Boolean {
        val finalResult = mutableListOf<Boolean>()
        filters.keys.forEach {
            val result = getImpact(filters[it]!!, event)
            if (result.contains(true)) finalResult.add(true) else finalResult.add(false)
        }
        return finalResult.indexOf(false) < 0
    }

    private fun getImpact(globalFilter: List<GlobalFilter>, event: MongoEvent): List<Boolean> {
        val result = globalFilter.map { filter ->
            val filterType = filter.globalFilterType
            val filterName = filter.name
            val filterOperator = filter.operator
            val filterValues = filter.values

            when (filterType) {
                (GlobalFilterType.Technographics) -> {
                    when (filterName) {
                        "browser" -> {
                            val broserDetails = event.system.browser!!.name
                            matchStringOperator(broserDetails, filterOperator, filterValues)
                        }
                        "device" -> {
                            val broserDetails = event.system.device!!.name
                            matchStringOperator(broserDetails, filterOperator, filterValues)
                        }
                        "os" -> {
                            val broserDetails = event.system.os!!.name
                            matchStringOperator(broserDetails, filterOperator, filterValues)
                        }
                        "application" -> {
                            val broserDetails = event.system.application!!.name
                            matchStringOperator(broserDetails, filterOperator, filterValues)
                        }
                        else -> false
                    }

                }
                (GlobalFilterType.AppFields) -> {
                    when (filterName) {
                        "appversion" -> {
                            val broserDetails = event.appfield!!.appversion ?: ""
                            matchStringOperator(broserDetails, filterOperator, filterValues)
                        }
                        "make" -> {
                            val broserDetails = event.appfield!!.make ?: ""
                            matchStringOperator(broserDetails, filterOperator, filterValues)
                        }
                        "model" -> {
                            val broserDetails = event.appfield!!.model ?: ""
                            matchStringOperator(broserDetails, filterOperator, filterValues)
                        }
                        "os" -> {
                            val broserDetails = event.appfield!!.os ?: ""
                            matchStringOperator(broserDetails, filterOperator, filterValues)
                        }
                        "sdkversion" -> {
                            val broserDetails = event.appfield!!.sdkversion ?: ""
                            matchStringOperator(broserDetails, filterOperator, filterValues)
                        }
                        else -> false
                    }
                }
                else -> false
            }
        }
        return result
    }

    fun checkGeographyFilter(metadata: Metadata, event: MongoEvent): Boolean {
        //if it return true mean it may be possible that we can add this user.
        val geofilter = metadata.segment.geographyFilters
        //we can improve data structure here
        if(geofilter.isEmpty()) return true
        geofilter.forEach {
            return (it.country!!.name == event.geogrophy!!.country!!) && (it.state!!.name == event.geogrophy!!.state) && (it.city!!.name == event.geogrophy!!.city)
        }
        return false
    }

    private fun matchStringOperator(eventValue: String, filterOperator: String, filterValues: List<String>): Boolean {
        return when (filterOperator) {
            StringOperator.Equals.name -> filterValues[0] == eventValue
            StringOperator.NotEquals.name -> filterValues[0] != eventValue
            StringOperator.Contains.name -> filterValues.contains(eventValue)
            StringOperator.DoesNotContain.name -> !filterValues.contains(eventValue)
            StringOperator.Exists.name -> true
            StringOperator.DoesNotExist.name -> true
            else -> false
        }
    }

    private fun matchNumberOperator(operatorName: String, metavalues: List<String>, eventvalues: Any): Boolean {
        val value = eventvalues as Int
        val intMetaValues = metavalues.map { it.toInt() }
        return when (NumberOperator.valueOf(operatorName)) {
            NumberOperator.Between -> value > intMetaValues[0] && value < intMetaValues[1]
            NumberOperator.Equals -> value == intMetaValues[0]
            NumberOperator.DoesNotExist -> true
            NumberOperator.Exists -> true
            NumberOperator.GreaterThan -> value > intMetaValues[0]
            NumberOperator.LessThan -> value < intMetaValues[0]
            NumberOperator.NONE -> true
            NumberOperator.NotEquals -> value != intMetaValues[0]
        }
    }

    private fun matchBooleanOperator(operatorName: String, metavalues: List<String>, eventvalues: Any): Boolean {
        val value = eventvalues as Boolean
        val intMetaValues = metavalues.map { it.toBoolean() }
        return value == intMetaValues[0]
    }

    private fun matchDateOperator(operatorName: String, metavalues: List<String>, eventvalues: Any): Boolean {
//        val value = eventvalues as String
//
//        when(DateOperator.valueOf(operatorName)){
//            DateOperator.Before -> ""
//                    DateOperator.After -> ""
//                    DateOperator.On -> ""
//                    DateOperator.Between
//                    DateOperator.InThePast
//                    DateOperator.WasExactly
//                    DateOperator.Today
//                    DateOperator.InTheFuture
//                    DateOperator.WillBeExactly
//                    DateOperator.Exists
//                    DateOperator.DoesNotExist
//                    DateOperator.AfterTime
//                    DateOperator.BetweenTime
//                    DateOperator.NONE
//        }
        return true
    }

//    fun createSegmentMetadata(segment: Segment, segmentId: Long, clientId: Long, status: String): Metadata {
//
//
//        fun buildMetaEvent(events: List<Event>): List<MetaEvent> {
//
//            val result: List<MetaEvent?> = events.map {
//                val metaEvent = when (it.dateFilter.operator) {
//                    DateOperator.After, DateOperator.Today -> {
//                        val metaEvent = MetaEvent()
//                        metaEvent.name = it.name
//                        metaEvent.operator = it.dateFilter.operator.name
//                        metaEvent.consider = true
//                        metaEvent.date = it.dateFilter.values
//                        metaEvent.property = it.propertyFilters
//                        metaEvent
//                    }
//                    DateOperator.Between -> {
//                        val lastDate = LocalDate.parse(it.dateFilter.values[1])
//                        val todayDate = LocalDate.now()
//                        if (lastDate.compareTo(todayDate) == 0) {
//                            val metaEvent = MetaEvent()
//                            metaEvent.name = it.name
//                            metaEvent.consider = true
//                            metaEvent.operator = it.dateFilter.operator.name
//                            metaEvent.date = it.dateFilter.values
//                            metaEvent.property = it.propertyFilters
//                            metaEvent
//                        } else {
//                            null
//                        }
//
//                    }
//                    DateOperator.On -> {
//                        val date = LocalDate.parse(it.dateFilter.values[0])
//                        val todayDate = LocalDate.now()
//                        if (date.compareTo(todayDate) == 0) {
//                            val metaEvent = MetaEvent()
//                            metaEvent.name = it.name
//                            metaEvent.consider = true
//                            metaEvent.operator = it.dateFilter.operator.name
//                            metaEvent.date = it.dateFilter.values
//                            metaEvent.property = it.propertyFilters
//                            metaEvent
//                        } else {
//                            null
//                        }
//                    }
//                    else -> null
//                }
//                metaEvent
//            }
//
//            return result.filterNotNull()
//        }
//
//        fun buildGlobalFilter(globalFilters: List<GlobalFilter>): Pair<Map<String, List<GlobalFilter>>, Map<String, List<GlobalFilter>>> {
//            val eventGlobalFilter = mutableListOf<GlobalFilter>()
//            val userGlobalFilter = mutableListOf<GlobalFilter>()
//            globalFilters.forEach {
//                when (it.globalFilterType) {
//                    GlobalFilterType.AppFields, GlobalFilterType.Technographics -> eventGlobalFilter.add(it)
//                    GlobalFilterType.Reachability, GlobalFilterType.Demographics, GlobalFilterType.UserProperties -> userGlobalFilter.add(it)
//                }
//            }
//            return Pair(eventGlobalFilter.groupBy { it.name }, userGlobalFilter.groupBy { it.name })
//        }
//
//        fun findCriteriaGroup(): SegmentCriteriaGroup {
//            val map = mutableMapOf<Boolean, MutableSet<Int>>()
////            val did = 1
////            val didnot = 2
////            val event = 3
////            val user = 4
//
//            if (segment.didEvents!!.events.isNotEmpty()) map.put(true, mutableSetOf(1))
//            if (segment.didNotEvents!!.events.isNotEmpty()) {
//                val v = map[true] ?: mutableSetOf(2)
//                v.add(2)
//                map.put(true, v)
//            }
//            if (segment.geographyFilters.isNotEmpty()) {
//                val v = map[true] ?: mutableSetOf(3)
//                v.add(3)
//                map.put(true, v)
//            }
//            if (segment.globalFilters.isNotEmpty()) {
//                val userGlobalFilter: GlobalFilter? = segment.globalFilters.find { (it.globalFilterType == GlobalFilterType.UserProperties || it.globalFilterType == GlobalFilterType.Demographics || it.globalFilterType == GlobalFilterType.Reachability) }
//                val eventGlobalFilter: GlobalFilter? = segment.globalFilters.find { (it.globalFilterType == GlobalFilterType.Technographics || it.globalFilterType == GlobalFilterType.AppFields) }
//                if (eventGlobalFilter != null || segment.geographyFilters.isNotEmpty()) {
//                    val v = map[true] ?: mutableSetOf(3)
//                    v.add(3)
//                    map.put(true, v)
//                }
//                if (userGlobalFilter != null) {
//                    val v = map[true] ?: mutableSetOf(4)
//                    v.add(4)
//                    map.put(true, v)
//                }
//
//            }
//            var group: SegmentCriteriaGroup = SegmentCriteriaGroup.NONE
//            map[true]?.let {
//                it.forEach {
//                    when (it) {
//                        1 -> group = SegmentCriteriaGroup.DID
//                        2 -> {
//                            if (group == SegmentCriteriaGroup.NONE) {
//                                group = SegmentCriteriaGroup.DIDNOT
//                            } else {
//                                group = SegmentCriteriaGroup.valueOf(group.name + "_DIDNOT")
//                            }
//                        }
//                        3 -> {
//                            if (group == SegmentCriteriaGroup.NONE) {
//                                group = SegmentCriteriaGroup.EVENTPROP
//                            } else {
//                                group = SegmentCriteriaGroup.valueOf(group.name + "_EVENTPROP")
//                            }
//                        }
//                        4 -> {
//                            if (group == SegmentCriteriaGroup.NONE) {
//                                group = SegmentCriteriaGroup.USERPROP
//                            } else {
//                                group = SegmentCriteriaGroup.valueOf(group.name + "_USERPROP")
//                            }
//                        }
//                    }
//                }
//            }
//
//            return group
//        }
//
//        fun isContainRelativeDate(events: List<Event>): Boolean {
//            events.forEach {
//                val operator = it.dateFilter.operator
//                val result = when (operator) {
//                    DateOperator.After, DateOperator.Today -> true
//                    DateOperator.On -> LocalDate.parse(it.dateFilter.values[0]).compareTo(LocalDate.now()) == 0
//                    DateOperator.Between -> LocalDate.parse(it.dateFilter.values[1]).compareTo(LocalDate.now()) == 0
//                    else -> false
//                }
//                if (result) return result
//            }
//            return false
//        }
//
//        val group = findCriteriaGroup()
//
//        fun isComputable(): Boolean {
//            //if we contain eventprop but did and did not date are absoulte then mark segment dead.
//            return when (group) {
//                SegmentCriteriaGroup.DID_DIDNOT, SegmentCriteriaGroup.DID_DIDNOT_EVENTPROP -> {
//                    if (isContainRelativeDate(segment.didEvents!!.events)) {
//                        true
//                    } else {
//                        isContainRelativeDate(segment.didNotEvents!!.events)
//                    }
//                }
//                SegmentCriteriaGroup.DIDNOT, SegmentCriteriaGroup.DIDNOT_EVENTPROP -> isContainRelativeDate(segment.didNotEvents!!.events)
//                SegmentCriteriaGroup.DID, SegmentCriteriaGroup.DID_EVENTPROP -> isContainRelativeDate(segment.didEvents!!.events)
//                SegmentCriteriaGroup.NONE -> false
//                else -> true
//            }
//        }
//
//        fun computeTriggerInfo(event: List<Event>): Boolean {
//            var compute: Boolean = false
//            event.forEach {
//                when (it.dateFilter.operator) {
//                    DateOperator.InThePast, DateOperator.WasExactly, DateOperator.Today -> compute = true
//                }
//            }
//            return compute
//        }
//
//        val metadata = Metadata()
//        with(metadata) {
//            id = segmentId
//            this.clientId = clientId
//            this.status = status
//            stopped = !isComputable()
//            this.segment = segment
//            criteriaGroup = findCriteriaGroup()
//            didEventsSize = segment.didEvents!!.events.size
//            didNotEventSize = segment.didNotEvents!!.events.size
//            conditionalOperator = segment.didEvents!!.joinCondition.conditionType
//            didEvents = buildMetaEvent(segment.didEvents!!.events)
//            didNotEvents = buildMetaEvent(segment.didNotEvents!!.events)
//            geoFilter = segment.geographyFilters
//        }
//        val (eventGlobalFilter, userGlobalFilter) = buildGlobalFilter(segment.globalFilters)
//        metadata.eventGlobalFilter = eventGlobalFilter
//        metadata.userGlobalFilter = userGlobalFilter
//        if (computeTriggerInfo(segment.didEvents!!.events) || computeTriggerInfo(segment.didNotEvents!!.events)) {
//            metadata.triggerInfo = createTriggerPointMetadata(metadata.segment)
//        }
//        return metadata
//    }

//    fun createTriggerPointMetadata(segment: Segment): TriggerInfo {
//        val result = findTriggerPoints(segment.didEvents!!.events, segment.didNotEvents!!.events, segment.creationDate)
//        val triggerInfo = TriggerInfo(null, segment.creationDate, null, false)
//        with(triggerInfo) {
//            this.timeZoneId = ZoneId.systemDefault()
//            this.triggerPoint = result
//        }
//        return triggerInfo
//    }

//    fun findTriggerPoint(events: List<Event>, creationTime: LocalDateTime, type: String): List<TriggerPoint> {
//        val list = mutableListOf<TriggerPoint?>()
//        events.forEachIndexed { index, it ->
//            var result = when (it.dateFilter.operator) {
//                DateOperator.WasExactly, DateOperator.Today, DateOperator.InThePast -> {
//                    var triggerPoint = TriggerPoint()
//                    with(triggerPoint) {
//                        unit = when (it.dateFilter.valueUnit) {
//                            Unit.mins -> {
//                                name = "$index+$type"
//                                lastExecutionPoint = creationTime
//                                interval = Integer.parseInt(it.dateFilter.values[0])
//                                Unit.mins
//                            }
//                            Unit.hours -> {
//                                name = "$index+$type"
//                                lastExecutionPoint = creationTime
//                                interval = Integer.parseInt(it.dateFilter.values[0])
//                                Unit.hours
//                            }
//                            else -> {
//                                name = "$index+$type"
//                                lastExecutionPoint = creationTime.toLocalDate().atStartOfDay()
//                                interval = 1
//                                Unit.days
//                            }
//                        }
//                    }
//                    triggerPoint
//                }
//                else -> null
//            }
//
//            list.add(result)
//        }
//        return list.filterNotNull()
//    }

//    fun findTriggerPoints(didEvents: List<Event>, didNotEvents: List<Event>, creationTime: LocalDateTime): List<TriggerPoint> {
//        var result = findTriggerPoint(didEvents, creationTime, "did")
//        var result1 = findTriggerPoint(didNotEvents, creationTime, "didnot")
//        var triggerPoints = mutableSetOf<TriggerPoint>()
//        triggerPoints.addAll(result)
//        triggerPoints.addAll(result1)
//        return triggerPoints.toList()
//    }


//    fun findNextTriggerPoint(triggerPoint: List<TriggerPoint>, timeZoneId: ZoneId): Pair<LocalDateTime, List<String>> {
//        val triggerPointDates = mutableListOf<LocalDateTime>()
//        val map = mutableMapOf<LocalDateTime, List<String>>()
//        triggerPoint.forEach {
//            when (it.unit) {
//                Unit.mins -> {
//                    val date = it.lastExecutionPoint?.plusMinutes(it.interval.toLong())
//                    triggerPointDates.add(date!!)
//                    if (map.contains(date)) {
//                        var value = map.get(date)!!.toMutableList()
//                        value.add(it.name)
//                        map.put(date, value)
//                    } else {
//                        map.put(date, listOf(it.name))
//                    }
//                }
//                Unit.hours -> {
//                    val date = it.lastExecutionPoint?.plusHours(it.interval.toLong())
//                    triggerPointDates.add(date!!)
//                    if (map.contains(date)) {
//                        var value = map.get(date)!!.toMutableList()
//                        value.add(it.name)
//                        map.put(date, value)
//                    } else {
//                        map.put(date, listOf(it.name))
//                    }
//                }
//                else -> {
//                    val date = it.lastExecutionPoint?.plusDays(it.interval.toLong())
//                    triggerPointDates.add(date!!)
//                    if (map.contains(date)) {
//                        var value = map.get(date)!!.toMutableList()
//                        value.add(it.name)
//                        map.put(date, value)
//                    } else {
//                        map.put(date, listOf(it.name))
//                    }
//                }
//            }
//        }
//        return Pair(triggerPointDates.toSortedSet().first(), map[triggerPointDates.toSortedSet().first()]!!)
//    }
//    /**
//     * This function return the interval in minute and time for which next segment computation is scheduled.
//     */
//    fun findNextScheduledDate(interval:Long,timePoint:List<Long>,timeZoneId: ZoneId,creationTime:LocalDateTime,today:Boolean):Pair<Long,LocalDateTime>{
//        val newTimePoint = sortedSetOf<Long>()
//        timePoint.forEach {
//            val quotients = (interval/it)+1
//            newTimePoint.add(it*quotients)
//        }
//        //when today present and date change to next day then schedule for start of day also.
//        var nextTime = creationTime.plusMinutes(newTimePoint.first())
//        if(today && nextTime.toLocalDate()> creationTime.toLocalDate()) nextTime = nextTime.toLocalDate().atStartOfDay(timeZoneId).toLocalDateTime()
//        return Pair(newTimePoint.first(),nextTime)
//    }
}


enum class SegmentCriteriaGroup {
    DID,
    DIDNOT,
    DID_DIDNOT,
    EVENTPROP,
    USERPROP,
    DID_EVENTPROP,
    DIDNOT_EVENTPROP,
    DID_USERPROP,
    DIDNOT_USERPROP,
    DID_EVENTPROP_USERPROP,
    DID_DIDNOT_EVENTPROP,
    EVENTPROP_USERPROP,
    DID_DIDNOT_USERPROP,
    DID_DIDNOT_EVENTPROP_USERPROP,
    NONE
}