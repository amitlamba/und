package com.und.livesegment.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.common.utils.DateUtils
import com.und.common.utils.loggerFor
import com.und.config.EventStream
import com.und.exception.UndException
import com.und.livesegment.model.EventMessage
import com.und.livesegment.model.LiveSegmentJobDetailProperties
import com.und.livesegment.model.LiveSegmentUser
import com.und.livesegment.model.LiveSegmentUserCheck
import com.und.livesegment.model.jpa.LiveSegment
import com.und.model.JobDescriptor
import com.und.model.JobDetail
import com.und.model.TriggerDescriptor
import com.und.repository.mongo.EventRepository
import com.und.service.SegmentParserCriteria
import com.und.service.SegmentService
import com.und.service.UserSettingsService
import com.und.web.model.DateFilter
import com.und.web.model.DateOperator
import com.und.web.model.Event
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Component
import java.util.*

@Component
class LiveSegmentProcessingService {

    companion object {
        val logger: Logger = loggerFor(LiveSegmentProcessingService::class.java)
    }

    @Autowired
    private lateinit var eventStream: EventStream

    @Autowired
    private lateinit var liveSegmentService: LiveSegmentService

    @Autowired
    private lateinit var segmentService: SegmentService

    @Autowired
    private lateinit var userSettingsService: UserSettingsService

    @Autowired
    private lateinit var eventRepository: EventRepository

    @Autowired
    private lateinit var segmentParserCriteria: SegmentParserCriteria

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var dateUtils: DateUtils


    @StreamListener("inEventForLiveSegment")
    fun processEventMessage(event: EventMessage){
        logger.info("Processing event: $event for segment checks")

        if(event.name == null){
            logger.info("No name found on the event.")
        }
        processStartEventChecks(event)
        processEventMessage(event)
    }

            /*
            * Start event checks were already performed while pushing the job so no need again
            * Check for end event filters in given lapsed period of time
            * If passed, push message
            */
    @StreamListener("inJobForLiveSegmentCheck")
    fun processJobForLiveSegmentCheck(params: LiveSegmentUserCheck){
        logger.info("Processing live segment checks: " + params)
        val possibleLiveSegments = this.liveSegmentService.findByClientIDAndEndEvent(params.clientId.toLong(), params.startEventName)

        possibleLiveSegments.forEach {
            liveSegment ->
            //we would handle cases of end event not done in last some interval only here. As the cases of end event happen within some interval must have already been processed
            //at the time of respective end event itself (in processEventMessage)
            if(liveSegment.endEventDone || liveSegment.endEventFilter == null || liveSegment.endEventFilter == "" || liveSegment.interval <= 0){
                return@forEach
            }

            val segment = this.segmentService.persistedSegmentById(liveSegment.segmentId)

            val endEventMatched = endEventHappenedInLastIntervalWithPropertiesMatched(liveSegment, params)
            if(endEventMatched) return@forEach

            val userPresentInSegment = this.segmentService.isUserPresentInSegment(segment, params.clientId.toLong(), params.userId)
            if(userPresentInSegment) sendJobToLiveSegmentQueue(params, liveSegment.id)
        }
    }

    @SendTo("outLiveSegment")
    private fun sendToLiveSegmentQueue(event: EventMessage, liveSegmentId: Long): LiveSegmentUser{
        logger.info("Pushing directly; details: ${event} for live-segment-id: $liveSegmentId")
        return LiveSegmentUser(liveSegmentId, event.clientId, event.userId, event.creationTime)
    }

    @SendTo("outLiveSegment")
    private fun sendJobToLiveSegmentQueue(params: LiveSegmentUserCheck, liveSegmentId: Long): LiveSegmentUser{
        logger.info("Pushing through job checks; details: ${params} for live-segment-id: $liveSegmentId")
        val startEventTime = dateUtils.convertDateTimeToDate(dateUtils.parseDateTime(params.startEventTime))
        return LiveSegmentUser(liveSegmentId, params.clientId.toLong(), params.userId, startEventTime)
    }

    @SendTo("scheduleJobSend")
    private fun sendToScheduleJob(event: EventMessage, liveSegment: LiveSegment): JobDescriptor{
        logger.info("Pushing scheduled job, details: $event for live-segment-id: ${liveSegment.id}")
        return buildJobDescriptor(event, liveSegment)
    }

    private fun processStartEventChecks(event: EventMessage){
        val possibleLiveSegments = this.liveSegmentService.findByClientIDAndStartEvent(event.clientId, event.name)

        possibleLiveSegments.forEach {
            liveSegment ->
            logger.info("Checking start event: ${event} for live-segment-id: ${liveSegment.id}")
            val segment = this.segmentService.persistedSegmentById(liveSegment.segmentId)

            val startEventMatched = startEventPropertiesMatched(liveSegment, event.eventId, event.userId, event.clientId)
            if(!startEventMatched) {
                logger.info("Start event not matched with ${event} for live-segment-id: ${liveSegment.id}")
                return@forEach
            }

            val userPresentInSegment = this.segmentService.isUserPresentInSegment(segment, event.clientId, event.userId)
            if(!userPresentInSegment) {
                logger.info("User checks not matched with ${event} for live-segment-id: ${liveSegment.id}")
                return@forEach
            }

            if(liveSegment.endEvent == null || liveSegment.endEvent == "")
                sendToLiveSegmentQueue(event, liveSegment.id)
            else
                sendToScheduleJob(event, liveSegment)
        }
    }

    private fun processEndEventChecks(event: EventMessage){
        val possibleLiveSegments = this.liveSegmentService.findByClientIDAndEndEvent(event.clientId, event.name)

        possibleLiveSegments.forEach {
            liveSegment ->
            logger.info("Checking end event: ${event} for live-segment-id: ${liveSegment.id}")
            if(!liveSegment.endEventDone || liveSegment.interval <= 0){
                logger.info("Not valid end event for live-segment-id: ${liveSegment.id}")
                return@forEach
            }

            val segment = this.segmentService.persistedSegmentById(liveSegment.segmentId)

            val endEventMatched = endEventPropertiesMatched(liveSegment, event.eventId, event.userId, event.clientId)
            if(!endEventMatched) {
                logger.info("End event not matched with ${event} for live-segment-id: ${liveSegment.id}")
                return@forEach
            }

            val startEventMatched = startEventHappenedInLastIntervalWithPropertiesMatched(liveSegment, event)
            if(!startEventMatched) {
                logger.info("Start event happened not matched with ${event} for live-segment-id: ${liveSegment.id}")
                return@forEach
            }

            val userPresentInSegment = this.segmentService.isUserPresentInSegment(segment, event.clientId, event.userId)
            if(userPresentInSegment) sendToLiveSegmentQueue(event, liveSegment.id)
        }
    }

    private fun startEventPropertiesMatched(liveSegment: LiveSegment, eventId: String, userId: String, clientId: Long): Boolean {
        val startEvent = objectMapper.readValue(liveSegment.startEventFilter, Event::class.java)
        if(startEvent == null || startEvent.propertyFilters == null || startEvent.propertyFilters.isEmpty()){
            logger.error("Live segment-id: ${liveSegment.id} without a start event is not valid")
            throw UndException("Live segment ${liveSegment.id} without a start event is not valid")
        }

        return eventPropertiesMatched(startEvent, eventId, userId, clientId)
    }

    private fun endEventPropertiesMatched(liveSegment: LiveSegment, eventId: String, userId: String, clientId: Long): Boolean {
        val endEvent = objectMapper.readValue(liveSegment.endEventFilter, Event::class.java)
        if(endEvent == null || endEvent.propertyFilters == null || endEvent.propertyFilters.isEmpty()){
            logger.error("Live segment ${liveSegment.id} without an end event is not eligible for end event processing")
            throw UndException("Live segment ${liveSegment.id} without an end event is not eligible for end event processing")
        }

        return eventPropertiesMatched(endEvent, eventId, userId, clientId)
    }

    private fun startEventHappenedInLastIntervalWithPropertiesMatched(liveSegment: LiveSegment, event: EventMessage): Boolean {
        val startEvent = objectMapper.readValue(liveSegment.startEventFilter, Event::class.java)
        if(startEvent == null){
            logger.error("Live segment ${liveSegment.id} without a start event is not valid")
            throw UndException("Live segment ${liveSegment.id} without a start event is not valid")
        }

        val dateFilter = DateFilter()
        dateFilter.operator = DateOperator.AfterTime
        dateFilter.values = listOf(dateUtils.convertDateToDateTime(event.creationTime).minusSeconds(liveSegment.interval).toString())
        startEvent.dateFilter = dateFilter

        return eventPropertiesMatched(startEvent, null, event.userId, event.clientId)
    }

    private fun endEventHappenedInLastIntervalWithPropertiesMatched(liveSegment: LiveSegment, params: LiveSegmentUserCheck): Boolean {
        val endEvent = objectMapper.readValue(liveSegment.endEventFilter, Event::class.java)
        if(endEvent == null){
            logger.error("Live segment ${liveSegment.id} without an end event is not eligible for end event processing")
            throw UndException("Live segment ${liveSegment.id} without an end event is not eligible for end event processing")
        }

        val dateFilter = DateFilter()
        dateFilter.operator = DateOperator.BetweenTime
        dateFilter.values = listOf(params.startEventTime, dateUtils.parseDateTime(params.startEventTime).plusSeconds(liveSegment.interval).toString())
        endEvent.dateFilter = dateFilter

        return eventPropertiesMatched(endEvent, null, params.userId, params.clientId.toLong())
    }

    private fun eventPropertiesMatched(event: Event, eventId: String?, userId: String, clientId: Long): Boolean {
        val tz = userSettingsService.getTimeZone()
        val propertyFilters = this.segmentParserCriteria.parsePropertyFilters(event, tz)

        val otherFilters = mutableListOf<Criteria>()
        otherFilters.add(Criteria.where("userId").`is`(userId))

        if(eventId != null) otherFilters.add(Criteria.where("id").`is`(eventId))
        if(event.dateFilter != null) otherFilters.add(this.segmentParserCriteria.parseDateFilter(event.dateFilter, tz))

        val matchOps = Aggregation.match(Criteria().andOperator(*otherFilters.toTypedArray(), *propertyFilters.toTypedArray()))
        val groupOps = Aggregation.group(Aggregation.fields().and(SegmentParserCriteria.Field.userId.name, SegmentParserCriteria.Field.userId.name))
        val aggregation = Aggregation.newAggregation(matchOps, groupOps)

        val idList = eventRepository.usersFromEvent(aggregation, clientId)
        if(!idList.isEmpty()){
            return true
        }

        return false
    }

    private fun buildJobDescriptor(event: EventMessage, liveSegment: LiveSegment): JobDescriptor {
        val properties = LiveSegmentJobDetailProperties()
        properties.clientId = liveSegment.clientID.toString()
        properties.segmentId = liveSegment.id.toString()
        properties.startEventId = event.eventId
        properties.startEventName = event.name
        properties.startEventTime = this.dateUtils.convertDateToDateTime(event.creationTime).toString()
        properties.userId = event.userId

        val jobDetail = JobDetail()
        jobDetail.jobName = "${liveSegment.id}-${event.eventId}-${event.userId}"
        jobDetail.jobGroupName = "${liveSegment.clientID}-${liveSegment.id}"
        jobDetail.properties = properties

        val jobDescriptor = JobDescriptor()
        jobDescriptor.clientId = liveSegment.clientID.toString()
        jobDescriptor.jobDetail = jobDetail
        jobDescriptor.triggerDescriptors = listOf(buildTriggerDescriptor(event.creationTime, liveSegment.interval))

        return jobDescriptor
    }

    private fun buildTriggerDescriptor(startTime: Date, interval: Long): TriggerDescriptor {
        val triggerDescriptor = TriggerDescriptor()
        triggerDescriptor.countTimes = 1
        triggerDescriptor.fireTime = dateUtils.convertDateToDateTime(startTime).plusSeconds(interval)
        return triggerDescriptor
    }


}