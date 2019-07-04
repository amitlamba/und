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

    fun buildMongoEvent(event: com.und.model.web.Event): com.und.model.mongo.Event {
//        var mongoEvent = MongoEvent(clientId = event.clientId, name = event.name)
//        mongoEvent.id=event.id
//        mongoEvent.userId = event.identity.userId
//        mongoEvent.timeZoneId = ZoneId.of(event.timeZone)
//        mongoEvent.creationTime = Date.from(Instant.ofEpochMilli(event.creationTime).atZone(ZoneId.of("UTC")).toInstant())
//        mongoEvent = mongoEvent.parseUserAgentString(event.agentString)
//        if (event.country != null && event.state != null && event.city != null) {
//            mongoEvent.geogrophy = Geogrophy(event.country, event.state, event.city)
//        } else {
//            event.ipAddress?.let {
//                mongoEvent.geogrophy = ipLocationRepository.getGeographyByIpAddress(it)
//            }
//        }
//        //FIXME hard coded charged
//        if ("charged".equals(event.name, ignoreCase = false)) {
//            mongoEvent.lineItem = event.lineItem
//            mongoEvent.lineItem.forEach { item ->
//                item.properties = mongoEventUtils.toDateInMap(item.properties)
//
//            }
//        }
//        //copy attributes
//        mongoEvent.attributes.putAll(mongoEventUtils.toDateInMap(event.attributes))
//        if (event.identity.idf == 1) {
//            mongoEvent.userIdentified = true
//        }


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

        mongoEvent.clientTime = ClientTimeNow(LocalDateTime.from(Instant.ofEpochMilli(event.creationTime).atZone(mongoEvent.timeZoneId)))
        mongoEvent.id = event.id
        mongoEvent.userId = event.identity.userId
        mongoEvent.sessionId = event.identity.sessionId
        mongoEvent.deviceId = event.identity.deviceId
        mongoEvent.notificationId = event.notificationId
        mongoEvent.appfield = event.appField

        with(mongoEvent.geoDetails) {
            ip = event.ipAddress
            //FIXME find a way to update coordinates

            var lat = event.latitude
            var long = event.longitude
            if (lat != null && long != null)
                mongoEvent.geoDetails.geolocation = GeoLocation("Point", Coordinate(lat.toDouble(), long.toDouble()))
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
            SegmentCriteriaGroup.USERPROP -> {
                //check it on push profile do nothing
            }
            else ->{
                //check exists check in redis and set time to live with key.
                val eventExists = segmentService.isEventExists(event.id!!)
                if(eventExists){
                    computeAndUpdateUser(metadata.segment, clientId, userId, segmentId)
                }else{
                    segmentService.saveEvent(event, clientId)
                    computeAndUpdateUser(metadata.segment, clientId, userId, segmentId)
                }

            }
        }
    }

    private fun computeAndUpdateUser(segment: Segment, clientId: Long, userId: String, segmentId: Long) {
        val result = segmentService.isUserPresentInSegment(segment, clientId, IncludeUsers.ALL, null, userId)
        if (result) {
            segmentService.addUserInSegment(userId, clientId, segmentId)
        } else {
            segmentService.removeUserFromSegment(userId, clientId, segmentId)
        }
    }
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