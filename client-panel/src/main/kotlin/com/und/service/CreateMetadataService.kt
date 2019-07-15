package com.und.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.config.EventStream
import com.und.model.*
import com.und.model.mongo.*
import com.und.repository.jpa.ClientSettingsRepository
import com.und.repository.mongo.SegmentMetadataRepository
import com.und.repository.mongo.SegmentUsersRepository
import com.und.web.model.Unit
import com.und.web.model.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class CreateMetadataService {

    @Autowired
    private lateinit var segmentService: SegmentService

    @Autowired
    private lateinit var segmentUsersRepository: SegmentUsersRepository

    @Autowired
    private lateinit var metadataRepository: SegmentMetadataRepository

    @Autowired
    private lateinit var eventStream: EventStream

    @Autowired
    private lateinit var objectMapper:ObjectMapper

    @Autowired
    private lateinit var clientSettingsRepository:ClientSettingsRepository

    companion object {
        val logger = LoggerFactory.getLogger(CreateMetadataService::class.java)
    }

    fun createSegmentMetadata(segment: Segment, segmentId: Long, clientId: Long, type: String): Metadata {


        fun buildMetaEvent(it: Event): MetaEvent {
            val metaEvent = MetaEvent()
            metaEvent.name = it.name
            metaEvent.operator = it.dateFilter.operator.name
            metaEvent.consider = true
            metaEvent.date = it.dateFilter.values
            metaEvent.property = it.propertyFilters
            return metaEvent
        }

        fun buildMetaEvent(events: List<Event>): List<MetaEvent> {

            val result: List<MetaEvent?> = events.map {
                val metaEvent = when (it.dateFilter.operator) {
                    DateOperator.After, DateOperator.Today -> {
                        buildMetaEvent(it)
                    }
                    DateOperator.Between -> {
                        val lastDate = LocalDate.parse(it.dateFilter.values[1])
                        val todayDate = LocalDate.now()
                        if (lastDate.compareTo(todayDate) == 0) {
                            buildMetaEvent(it)
                        } else {
                            null
                        }

                    }
                    DateOperator.On -> {
                        val date = LocalDate.parse(it.dateFilter.values[0])
                        val todayDate = LocalDate.now()
                        if (date.compareTo(todayDate) == 0) {
                            buildMetaEvent(it)
                        } else {
                            null
                        }
                    }
                    else -> null
                }
                metaEvent
            }

            return result.filterNotNull()
        }

        fun buildGlobalFilter(globalFilters: List<GlobalFilter>): Pair<Map<String, List<GlobalFilter>>, Map<String, List<GlobalFilter>>> {
            val eventGlobalFilter = mutableListOf<GlobalFilter>()
            val userGlobalFilter = mutableListOf<GlobalFilter>()
            globalFilters.forEach {
                when (it.globalFilterType) {
                    GlobalFilterType.AppFields, GlobalFilterType.Technographics -> eventGlobalFilter.add(it)
                    GlobalFilterType.Reachability, GlobalFilterType.Demographics, GlobalFilterType.UserProperties -> userGlobalFilter.add(it)
                }
            }
            return Pair(eventGlobalFilter.groupBy { it.name }, userGlobalFilter.groupBy { it.name })
        }

        fun findCriteriaGroup(): SegmentCriteriaGroup {
            val map = mutableMapOf<Boolean, MutableSet<Int>>()
//            val did = 1
//            val didnot = 2
//            val event = 3
//            val user = 4

            if (segment.didEvents!!.events.isNotEmpty()) map.put(true, mutableSetOf(1))
            if (segment.didNotEvents!!.events.isNotEmpty()) {
                val v = map[true] ?: mutableSetOf(2)
                v.add(2)
                map.put(true, v)
            }
            if (segment.geographyFilters.isNotEmpty()) {
                val v = map[true] ?: mutableSetOf(3)
                v.add(3)
                map.put(true, v)
            }
            if (segment.globalFilters.isNotEmpty()) {
                val userGlobalFilter: GlobalFilter? = segment.globalFilters.find { (it.globalFilterType == GlobalFilterType.UserProperties || it.globalFilterType == GlobalFilterType.Demographics || it.globalFilterType == GlobalFilterType.Reachability) }
                val eventGlobalFilter: GlobalFilter? = segment.globalFilters.find { (it.globalFilterType == GlobalFilterType.Technographics || it.globalFilterType == GlobalFilterType.AppFields) }
                if (eventGlobalFilter != null || segment.geographyFilters.isNotEmpty()) {
                    val v = map[true] ?: mutableSetOf(3)
                    v.add(3)
                    map.put(true, v)
                }
                if (userGlobalFilter != null) {
                    val v = map[true] ?: mutableSetOf(4)
                    v.add(4)
                    map.put(true, v)
                }

            }
            var group: SegmentCriteriaGroup = SegmentCriteriaGroup.NONE
            map[true]?.let {
                it.forEach {
                    when (it) {
                        1 -> group = SegmentCriteriaGroup.DID
                        2 -> {
                            if (group == SegmentCriteriaGroup.NONE) {
                                group = SegmentCriteriaGroup.DIDNOT
                            } else {
                                group = SegmentCriteriaGroup.valueOf(group.name + "_DIDNOT")
                            }
                        }
                        3 -> {
                            if (group == SegmentCriteriaGroup.NONE) {
                                group = SegmentCriteriaGroup.EVENTPROP
                            } else {
                                group = SegmentCriteriaGroup.valueOf(group.name + "_EVENTPROP")
                            }
                        }
                        4 -> {
                            if (group == SegmentCriteriaGroup.NONE) {
                                group = SegmentCriteriaGroup.USERPROP
                            } else {
                                group = SegmentCriteriaGroup.valueOf(group.name + "_USERPROP")
                            }
                        }
                    }
                }
            }

            return group
        }

        fun isContainRelativeDate(events: List<Event>): Boolean {
            events.forEach {
                val operator = it.dateFilter.operator
                val result = when (operator) {
                    DateOperator.After, DateOperator.Today -> true
                    DateOperator.On -> LocalDate.parse(it.dateFilter.values[0]).compareTo(LocalDate.now()) == 0
                    DateOperator.Between -> LocalDate.parse(it.dateFilter.values[1]).compareTo(LocalDate.now()) == 0
                    else -> false
                }
                if (result) return result
            }
            return false
        }

        val group = findCriteriaGroup()

        fun isComputable(): Boolean {
            //if we contain event prop but did and did not criteria date are absolute then mark segment dead.
            return when (group) {
                SegmentCriteriaGroup.DID_DIDNOT, SegmentCriteriaGroup.DID_DIDNOT_EVENTPROP -> {
                    if (isContainRelativeDate(segment.didEvents!!.events)) {
                        true
                    } else {
                        isContainRelativeDate(segment.didNotEvents!!.events)
                    }
                }
                SegmentCriteriaGroup.DIDNOT, SegmentCriteriaGroup.DIDNOT_EVENTPROP -> isContainRelativeDate(segment.didNotEvents!!.events)
                SegmentCriteriaGroup.DID, SegmentCriteriaGroup.DID_EVENTPROP -> isContainRelativeDate(segment.didEvents!!.events)
                SegmentCriteriaGroup.NONE -> false
                else -> true
            }
        }

        fun computeTriggerInfo(event: List<Event>): Boolean {
            var compute: Boolean = false
            event.forEach {
                when (it.dateFilter.operator) {
                    DateOperator.InThePast, DateOperator.WasExactly, DateOperator.Today -> compute = true
                }
            }
            return compute
        }

        val metadata = Metadata()
        with(metadata) {
            id = segmentId
            this.clientId = clientId
            this.type = type
            stopped = !isComputable()
            this.segment = segment
            criteriaGroup = group
            creationTime = segment.creationDate
            didEventsSize = segment.didEvents!!.events.size
            didNotEventSize = segment.didNotEvents!!.events.size
            conditionalOperator = segment.didEvents!!.joinCondition.conditionType
            didEvents = buildMetaEvent(segment.didEvents!!.events)
            didNotEvents = buildMetaEvent(segment.didNotEvents!!.events)
            geoFilter = segment.geographyFilters
        }
        val (eventGlobalFilter, userGlobalFilter) = buildGlobalFilter(segment.globalFilters)
        metadata.eventGlobalFilter = eventGlobalFilter
        metadata.userGlobalFilter = userGlobalFilter
        if (computeTriggerInfo(segment.didEvents!!.events) || computeTriggerInfo(segment.didNotEvents!!.events)) {
            metadata.triggerInfo = createTriggerPointMetadata(metadata.segment)
        }
        return metadata
    }
    fun createTriggerPointMetadata(segment: Segment): TriggerInfo {
        val result = findTriggerPoints(segment.didEvents!!.events, segment.didNotEvents!!.events, segment.creationDate)
        val triggerInfo = TriggerInfo(null, segment.creationDate, null, true)
        with(triggerInfo) {
            this.timeZoneId = ZoneId.systemDefault()
            this.triggerPoint = result
        }
        return triggerInfo
    }

    fun findTriggerPoint(events: List<Event>, creationTime: LocalDateTime, type: String): List<TriggerPoint> {
        val list = mutableListOf<TriggerPoint?>()
        events.forEachIndexed { index, it ->
            var result = when (it.dateFilter.operator) {
                DateOperator.WasExactly, DateOperator.Today, DateOperator.InThePast -> {
                    var triggerPoint = TriggerPoint()
                    with(triggerPoint) {
                        unit = when (it.dateFilter.valueUnit) {
                            Unit.mins -> {
                                name = "$index+$type"
                                lastExecutionPoint = creationTime
                                interval = Integer.parseInt(it.dateFilter.values[0])
                                Unit.mins
                            }
                            Unit.hours -> {
                                name = "$index+$type"
                                lastExecutionPoint = creationTime
                                interval = Integer.parseInt(it.dateFilter.values[0])
                                Unit.hours
                            }
                            else -> {
                                name = "$index+$type"
                                lastExecutionPoint = creationTime.toLocalDate().atStartOfDay()
                                interval = 1
                                Unit.days
                            }
                        }
                    }
                    triggerPoint
                }
                else -> null
            }

            list.add(result)
        }
        return list.filterNotNull()
    }

    fun findTriggerPoints(didEvents: List<Event>, didNotEvents: List<Event>, creationTime: LocalDateTime): List<TriggerPoint> {
        var result = findTriggerPoint(didEvents, creationTime, "did")
        var result1 = findTriggerPoint(didNotEvents, creationTime, "didnot")
        var triggerPoints = mutableSetOf<TriggerPoint>()
        triggerPoints.addAll(result)
        triggerPoints.addAll(result1)
        return triggerPoints.toList()
    }

    fun findNextTriggerPoint(triggerPoint: List<TriggerPoint>, timeZoneId: ZoneId): Pair<LocalDateTime, List<String>> {
        val triggerPointDates = mutableListOf<LocalDateTime>()
        val map = mutableMapOf<LocalDateTime, List<String>>()
        triggerPoint.forEach {
            when (it.unit) {
                Unit.mins -> {
                    val date = it.lastExecutionPoint?.plusMinutes(it.interval.toLong())
                    triggerPointDates.add(date!!)
                    if (map.contains(date)) {
                        var value = map.get(date)!!.toMutableList()
                        value.add(it.name)
                        map.put(date, value)
                    } else {
                        map.put(date, listOf(it.name))
                    }
                }
                Unit.hours -> {
                    val date = it.lastExecutionPoint?.plusHours(it.interval.toLong())
                    triggerPointDates.add(date!!)
                    if (map.contains(date)) {
                        var value = map.get(date)!!.toMutableList()
                        value.add(it.name)
                        map.put(date, value)
                    } else {
                        map.put(date, listOf(it.name))
                    }
                }
                else -> {
                    val date = it.lastExecutionPoint?.plusDays(it.interval.toLong())
                    triggerPointDates.add(date!!)
                    if (map.contains(date)) {
                        var value = map.get(date)!!.toMutableList()
                        value.add(it.name)
                        map.put(date, value)
                    } else {
                        map.put(date, listOf(it.name))
                    }
                }
            }
        }
        return Pair(triggerPointDates.toSortedSet().first(), map[triggerPointDates.toSortedSet().first()]!!)
    }

    fun updateTriggerInfo(newTriggerInfo: TriggerInfo,existingTriggerInfo: TriggerInfo,tiggerName:List<String>):TriggerInfo{

        tiggerName.forEach { name ->
            //finding the tigger point whose last execution point should be changed.
            val result1: List<TriggerPoint> = existingTriggerInfo.triggerPoint.filter {
                it.name == name
            }
            //updating the last execution date of those trigger point.
            result1.forEach {
                it.lastExecutionPoint = newTriggerInfo.nextTriggerPoint
            }
            val newTriggerPoint = mutableListOf<TriggerPoint>()
            newTriggerPoint.addAll(result1)
            val result2: List<TriggerPoint> = existingTriggerInfo.triggerPoint.filter {
                it.name != name
            }
            newTriggerPoint.addAll(result2)
            newTriggerInfo.triggerPoint = newTriggerPoint
        }
        return newTriggerInfo
    }

    /**
     * This method execute just after segment save.
     * It compute segment for first time and save the segment users list into segment_users collection after that
     * This method schedule a job if trigger point present in metadata...
     */
    @StreamListener("inSegment")
    fun segmentPostProcessing(jpaSegment: com.und.model.jpa.Segment){
        logger.info("Computing the segment and creating metadata")
        val segment:Segment = buildWebSegmentWithFilters(jpaSegment) //building webSegment from jpa segment
        val timeZoneId = clientSettingsRepository.findByClientID(jpaSegment.clientID!!)?.timezone?:"UTC"
        var error:Boolean = false
        var message:String? = null
        try{
            val users = segmentService.segmentUserIds(segment,jpaSegment.clientID!!,IncludeUsers.ALL)
            val segmentUsers = SegmentUsers()
            with(segmentUsers){
                this.segmentId =jpaSegment.id
                this.clientId = jpaSegment.clientID
                this.users = users.toSet()
            }
            segmentUsersRepository.save(segmentUsers)
        }catch (ex:Exception){
            logger.error("Exception occurred ${ex.localizedMessage}")
            error = true
            message = ex.localizedMessage
        }

        //creating metadata  next trigger point date is that date fro which segment computation is scheduled but not completed yet.
        val metadata = createSegmentMetadata(segment,jpaSegment.id!!,jpaSegment.clientID!!,"past")
        metadata.triggerInfo?.let {
            val scheduleDate = findNextTriggerPoint(it.triggerPoint,it.timeZoneId)
            var triggerInfo = TriggerInfo(segment.creationDate,scheduleDate.first,message,error)
            triggerInfo = updateTriggerInfo(triggerInfo,it,scheduleDate.second)
            metadata.triggerInfo = triggerInfo
            val jobDescriptor = buildSegmentJobDescriptor(JobDescriptor.Action.CREATE,jpaSegment.clientID!!, ZoneId.of(timeZoneId),segment.name,jpaSegment.id!!,"past",scheduleDate.first)
            scheduleSegmentJob(jobDescriptor)
        }
        metadataRepository.save(metadata)
        logger.debug("Metadata saved.")

    }

    /**
     * This method is listening from scheduler
     */
    @StreamListener("inComputeSegment")
    fun computeSegment(computeSegment:ComputeSegment){
        logger.info("Segment is scheduled for computing.")
        val metadata = metadataRepository.findById(computeSegment.segmentId)
        var error:Boolean = false
        var message:String? = null
        metadata.ifPresent{ metadata ->

        try{
            val users = segmentService.segmentUserIds(metadata.segment,metadata.clientId!!,IncludeUsers.ALL)
            val segmentUsers = SegmentUsers()
            with(segmentUsers){
                this.segmentId =segmentId
                this.clientId = clientId
                this.users = users.toSet()
            }
            segmentUsersRepository.save(segmentUsers)
        }catch (ex:Exception){
            error = true
            message = ex.localizedMessage
        }
        metadata.triggerInfo?.let {
            val scheduleDate = findNextTriggerPoint(it.triggerPoint,it.timeZoneId)
            var triggerInfo = TriggerInfo(it.nextTriggerPoint,scheduleDate.first,message,error)
            triggerInfo = updateTriggerInfo(triggerInfo,it,scheduleDate.second)
            metadata.triggerInfo = triggerInfo
            logger.info("Next segment schedule date is ${scheduleDate.first}")
            val jobDescriptor = buildSegmentJobDescriptor(JobDescriptor.Action.CREATE,computeSegment.clientId!!, computeSegment.timeZoneId,computeSegment.segmentName?:"",computeSegment.segmentId!!,metadata.type,scheduleDate.first)
            scheduleSegmentJob(jobDescriptor)
        }
        metadataRepository.save(metadata)
        }
    }

    private fun buildWebSegmentWithFilters(segment: com.und.model.jpa.Segment): Segment {
        val websegment = objectMapper.readValue(segment.data, Segment::class.java)
        with(websegment) {
            id = segment.id
            name = segment.name
            type = segment.type
        }
        return websegment
    }
    fun scheduleSegmentJob(jobDescriptor:JobDescriptor){
        logger.debug("Job scheduled for ${jobDescriptor.clientId}")
        eventStream.outSegmentScheduleJob().send(MessageBuilder.withPayload(jobDescriptor).build())
    }
    fun buildSegmentJobDescriptor(action:JobDescriptor.Action,clientId: Long,timeZoneId: ZoneId,segmentName:String,segmentId:Long,type:String,scheduleDate:LocalDateTime):JobDescriptor{

        fun buildJobDetails(segmentName:String,segmentId:Long,type: String,clientId: Long):JobDetail{
            val segmentProperties = SegmentJobProperties(segmentName, segmentId, type)
            val jobDetails = JobDetail()
            with(jobDetails){
                jobName = "${segmentName}-$segmentId"
                jobGroupName = "$clientId-$segmentId"
                properties = segmentProperties
                jobType = JobDetail.JobType.SEGMENT

            }
            return jobDetails
        }

        fun buildTriggerDescriptor(time:LocalDateTime):List<TriggerDescriptor>{
            val triggerDescriptor = TriggerDescriptor()
            with(triggerDescriptor){
                fireTime = time
            }
            return listOf(triggerDescriptor)
        }
        val jobDescriptor = JobDescriptor()
        with(jobDescriptor){
            this.timeZoneId = timeZoneId
            this.clientId = clientId.toString()
            this.action = action
            this.jobDetail = buildJobDetails(segmentName, segmentId, type, clientId)
            this.triggerDescriptors = buildTriggerDescriptor(scheduleDate)
        }
        return jobDescriptor
    }
}