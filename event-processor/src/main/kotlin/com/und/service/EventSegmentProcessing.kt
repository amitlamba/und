package com.und.service


import com.und.model.*
import com.und.model.mongo.Geogrophy
import com.und.model.mongo.Metadata
import com.und.model.web.EventMessage
import com.und.repository.mongo.IpLocationRepository
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
import java.time.Instant
import java.time.ZoneId
import java.util.*


@Service
class EventSegmentProcessing {

    @Autowired
    private lateinit var dateUtils: DateUtils

    @Autowired
    private lateinit var ipLocationRepository :IpLocationRepository

    @Autowired
    private lateinit var mongoEventUtils : MongoEventUtils

    @StreamListener(Constants.PROCESS_SEGMENT)
    fun processSegment(event: WebEvent) {
        //find all segments metadata for this clientid and not dead from cache
        //divide live segment for high priority processing. to improve performance store them separately redis key live_clientId,past_clientId
        //build mongo event
        var mongoEvent = MongoEvent(clientId = event.clientId, name = event.name)
        mongoEvent.timeZoneId = ZoneId.of(event.timeZone)
        mongoEvent.creationTime= Date.from(Instant.ofEpochMilli(event.creationTime).atZone(ZoneId.of("UTC")).toInstant())
        mongoEvent = mongoEvent.parseUserAgentString(event.agentString)
        if(event.country != null && event.state != null && event.city != null){
            mongoEvent.geogrophy = Geogrophy(event.country,event.state,event.city)
        }else{
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
        val liveSegments = listOf<Metadata>()
        liveSegments.forEach {
            checkEventEffectOnSegment(mongoEvent, Metadata())
        }
        //send for live procssing
        sendForLiveProcessing()
        val pastSegments = listOf<Metadata>()
        pastSegments.forEach {
            checkEventEffectOnSegment(mongoEvent, Metadata())
        }
    }

    fun sendForLiveProcessing(mongoEvent:com.und.model.mongo.Event){
        eventStream.outEventForLiveSegment().send(MessageBuilder.withPayload(buildEventForLiveSegment(mongoEvent)).build())
    }
    fun buildEventForLiveSegment(fromEvent: com.und.model.mongo.Event): EventMessage {
        val eventId = fromEvent.id
        if (eventId != null) {
            return EventMessage(eventId, fromEvent.clientId, fromEvent.userId, fromEvent.name, fromEvent.creationTime, fromEvent.userIdentified)
        } else {
            //throw EventNotFoundException("Event with null id")
        }

    }
    fun checkEventEffectOnSegment(event: MongoEvent, metadata: Metadata) {
        //if a segment contain did or did not then chek eventname is it effect ot not.
        when (metadata.criteriaGroup) {
            SegmentCriteriaGroup.DID -> {
                //if return true then add userID in list no need computation.
            }
            SegmentCriteriaGroup.DID_DIDNOT -> {
                //if return true then remove userID in list no need computation.
            }
            SegmentCriteriaGroup.EVENTPROP -> {
                //it return true then add userId
            }
            SegmentCriteriaGroup.USERPROP -> {
                //check it on push profile
            }
            else -> {
                //check is this event affect global filter
                val eventProp = checkEventProperties(metadata.segment!!.globalFilters,event)
                //check is this event affect geography filter
                val geoProp = checkGeographyFilter(metadata,event)
                //if they return false then don't compute. because this event user is never included if its already present then ok.
                if (!eventProp || !geoProp) {
                    //check is this event affect did event
                    if (checkEvent(metadata.segment!!.didEvents!!,event)) {
                        //compute
                    } else {
                        //check is this event affect did not event
                        if (checkEvent(metadata.segment!!.didNotEvents!!,event)) {
                            //compute
                        }
                    }
                }
            }
        }
    }

    fun checkEvent(metadata: DidEvents, event: MongoEvent): Boolean {

        val tz = ZoneId.of("UTC")
        val filterResult = mutableListOf<Boolean>()
        metadata.let {
            var events = metadata.events
            var eventAttributes = event.attributes
            events.forEach { metaEvent ->
                if (metaEvent.name == event!!.name) {
                    val result: Boolean = when (metaEvent.dateFilter.operator) {
                        DateOperator.Before -> {
                            val dates = metaEvent.dateFilter.values
                            val date = dateUtils.getStartOfDay(dates.first(), tz)
                            val eventCreationTime = event.creationTime
                            when {
                                eventCreationTime < date -> true
                                else -> false
                            }
                        }
                        DateOperator.Between -> {
                            val dates = metaEvent.dateFilter.values
                            val startDate = dateUtils.getStartOfDay(dates.first(), tz)
                            val endDate = dateUtils.getMidnight(dates.last(), tz)
                            val eventCreationTime = event.creationTime
                            when {
                                (eventCreationTime.after(startDate) && eventCreationTime.before(endDate)) || eventCreationTime.equals(startDate) || eventCreationTime.equals(endDate) -> true
                                else -> false
                            }
                        }
                        DateOperator.After -> {
                            val dates = metaEvent.dateFilter.values
                            val date = dateUtils.getMidnight(dates.first(), tz)
                            val eventCreationTime = event.creationTime
                            when {
                                eventCreationTime > date -> true
                                else -> false
                            }
                        }
                        DateOperator.On -> {
                            val dates = metaEvent.dateFilter.values
                            val date = dateUtils.getStartOfDay(dates.first(), tz)
                            val eventCreationTime = event.creationTime
                            when {
                                eventCreationTime == date -> true
                                else -> false
                            }
                        }
                        else -> true  //in case of relative date we need to compute always
                    }
                    if (result) {
                        //add event filter check
                        val propResult = checkEventAttributes(metaEvent.propertyFilters, eventAttributes)
                        if (propResult) filterResult.add(propResult) else filterResult.add(propResult)

                    } else {
                        filterResult.add(result)
                    }
                } else {
                    filterResult.add(false)
                }

            }
        }
        return filterResult.contains(true)
    }

    fun checkEventAttributes(metaAttibutes: List<PropertyFilter>, eventAttributes: HashMap<String, Any>): Boolean {
        val groupByNameAttr = metaAttibutes.groupBy { it.name }   // its beter to store metadata in groupby format.
        val filterResult = mutableListOf<Boolean>()
        groupByNameAttr.forEach { key,propertyFilters->
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
                            matchStringOperator(eventAttributes[filter.name]!! as String,filter.operator, filter.values)
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

//    fun checkDidNotEvent(metadata: DidEvents?, event: Event): Boolean {
//        //TODO  nere no need to compute segment if did events and event properties have no criteria to compute then
//        //TODo here we just remove userId if criteria match else do nothing.
//
//        //TODO get timezone of client
//        return false
//    }

    fun checkEventProperties(globalFilter: List<GlobalFilter>, event: MongoEvent): Boolean {
        //TODO if userID already present do nothing else check and compute
        // if all true return then compute segment
        //filter only techno and app field global filters. we create our metadata in this way that we dont need to filter.

        val filters: Map<String, List<GlobalFilter>> = globalFilter.groupBy {
            it.name
        }
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
                            val broserDetails = event!!.system.browser!!.name
                            matchStringOperator(broserDetails, filterOperator, filterValues)
                        }
                        "device" -> {
                            val broserDetails = event!!.system.device!!.name
                            matchStringOperator(broserDetails, filterOperator, filterValues)
                        }
                        "os" -> {
                            val broserDetails = event!!.system.os!!.name
                            matchStringOperator(broserDetails, filterOperator, filterValues)
                        }
                        "application" -> {
                            val broserDetails = event!!.system.application!!.name
                            matchStringOperator(broserDetails, filterOperator, filterValues)
                        }
                        else -> false
                    }

                }
                (GlobalFilterType.AppFields) -> {
                    when (filterName) {
                        "appversion" -> {
                            val broserDetails = event!!.appfield!!.appversion
                            matchStringOperator(broserDetails, filterOperator, filterValues)
                        }
                        "make" -> {
                            val broserDetails = event!!.appfield!!.make
                            matchStringOperator(broserDetails, filterOperator, filterValues)
                        }
                        "model" -> {
                            val broserDetails = event!!.appfield!!.model
                            matchStringOperator(broserDetails, filterOperator, filterValues)
                        }
                        "os" -> {
                            val broserDetails = event!!.appfield!!.os
                            matchStringOperator(broserDetails, filterOperator, filterValues)
                        }
                        "sdkversion" -> {
                            val broserDetails = event!!.appfield!!.sdkversion
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
        val geofilter = metadata.segment!!.geographyFilters
        //we can improve data structure here
        geofilter.forEach {
            return (it.country!!.name == event.geogrophy!!.country!!) && (it.state!!.name == event.geogrophy!!.state) && (it.city!!.name == event.geogrophy!!.city)
        }
        return false
    }

    private fun matchStringOperator(details: String?, filterOperator: String, filterValues: List<String>): Boolean {
        return when (filterOperator) {
            StringOperator.Equals.name -> filterValues[0] == details!!
            StringOperator.NotEquals.name -> filterValues[0] != details!!
            StringOperator.Contains.name -> filterValues.contains(details!!)
            StringOperator.DoesNotContain.name -> !filterValues.contains(details!!)
            StringOperator.Exists.name -> details != null
            StringOperator.DoesNotExist.name -> details == null
            else -> false
        }
    }

    private fun matchNumberOperator(operatorName: String, metavalues: List<String>, eventvalues: Any): Boolean {
        val value = eventvalues as Int
        val intMetaValues = metavalues.map { it.toInt() }
        return when(NumberOperator.valueOf(operatorName)){
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

    private fun matchBooleanOperator(operatorName: String, metavalues: List<String>, eventvalues: Any): Boolean{
        val value = eventvalues as Boolean
        val intMetaValues = metavalues.map { it.toBoolean() }
        return if(value == intMetaValues[0]) true else false
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

//    fun createMetadata(segment: Segment){
//
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
    //DID_USERPROP,
    //DIDNOT_USERPROP,
    DID_DIDNOT_EVENTPROP
}