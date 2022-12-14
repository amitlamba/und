package com.und.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.model.IncludeUsers
import com.und.model.UpdateIdentity
import com.und.model.mongo.ClientTimeNow
import com.und.model.mongo.Coordinate
import com.und.model.mongo.GeoLocation
import com.und.model.mongo.Geogrophy
import com.und.model.web.Event
import com.und.repository.mongo.EventRepository
import com.und.repository.mongo.EventUpdateRepository
import com.und.repository.mongo.IpLocationRepository
import com.und.repository.mongo.MetadataRepository
import com.und.service.segmentquerybuilder.SegmentService
import com.und.utils.*
import com.und.model.mongo.Event as MongoEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@Service
class EventSaveService {

    @Autowired
    private lateinit var ipLocationRepository : IpLocationRepository

    @Autowired
    private lateinit var eventRepository:EventRepository

    @Autowired
    private lateinit var eventUpdateRepository : EventUpdateRepository

    @Autowired
    private lateinit var mongoEventUtils : MongoEventUtils

    @Autowired
    private lateinit var segmentService:SegmentService

    @Autowired
    private lateinit var metadataRepository:MetadataRepository

    @StreamListener(Constants.SAVE_EVENT)
    fun saveEvent(event: Event){

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
        eventRepository.insertIfNotExistsElseUpdate(mongoEvent,mongoEvent.clientId)
    }

    fun updateEventWithUserIdentity(identity: UpdateIdentity) {
        //tenantProvider.setTenat(identity.clientId.toString())
        eventUpdateRepository.updateEventsWithIdentityMatching(identity)
        //Recomputing segments after identity update.
        val metadataGroup = metadataRepository.findByClientId(identity.clientId.toLong())
        metadataGroup.forEach {
            // computing only those segments which are not dead.
            if(!it.stopped){
                val result = segmentService.isUserPresentInSegment(it.segment, identity.clientId.toLong(), IncludeUsers.ALL, null, identity.update)
                if (result) {
                    segmentService.addUserInSegment(identity.update, identity.clientId.toLong(), it.id!!)
                } else {
                    segmentService.removeUserFromSegment(identity.update, identity.clientId.toLong(), it.id!!)
                }
            }
        }


    }

//    fun buildEventForLiveSegment(fromEvent: com.und.model.mongo.Event): EventMessage {
//        val eventId = fromEvent.id
//        if (eventId != null) {
//            return EventMessage(eventId, fromEvent.clientId, fromEvent.userId, fromEvent.name, fromEvent.creationTime, fromEvent.userIdentified)
//        } else {
//            throw EventNotFoundException("Event with null id")
//        }
//
//    }
}