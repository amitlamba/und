package com.und.service.eventapi

import com.und.common.utils.MetadataUtil
import com.und.eventapi.utils.copyToMongo
import com.und.eventapi.utils.ipAddr
import com.und.config.EventStream
import com.und.model.mongo.eventapi.EventMetadata
import com.und.repository.mongo.EventMetadataRepository
import com.und.repository.mongo.EventRepository
import com.und.repository.mongo.EventUserRepository
import com.und.security.utils.AuthenticationUtils
import com.und.security.utils.TenantProvider
import com.und.web.model.eventapi.Event
import com.und.web.model.eventapi.Identity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import javax.servlet.http.HttpServletRequest
import com.und.model.mongo.eventapi.Event as MongoEvent

@Service
class EventService {


    @Autowired
    private lateinit var tenantProvider: TenantProvider

    @Autowired
    private lateinit var eventRepository: EventRepository

    @Autowired
    private lateinit var eventMetadataRepository: EventMetadataRepository

    @Autowired
    private lateinit var eventUserRepository: EventUserRepository

    @Autowired
    private lateinit var eventStream: EventStream

    fun findByName(name: String): List<MongoEvent> = eventRepository.findByName(name)


    fun toKafka(event: Event): Boolean = eventStream.outEvent().send(MessageBuilder.withPayload(event).build())


    @StreamListener("inEvent")
    @SendTo("outEvent")
    fun save(event: Event) {

        saveEvent(event)

    }

    fun saveEvent(event: Event): String? {
        val clientId = event.clientId
        tenantProvider.setTenat(clientId.toString())
        val mongoEvent = event.copyToMongo()
        mongoEvent.clientTime.hour
        val eventMetadata = buildMetadata(mongoEvent)
        eventMetadataRepository.save(eventMetadata)
        //FIXME add to metadata
        val saved = eventRepository.insert(mongoEvent)
        return saved.id
    }

    private fun buildMetadata(event: MongoEvent): EventMetadata {
        val metadata = eventMetadataRepository.findByName(event.name) ?: EventMetadata()
        metadata.name = event.name
        val properties = MetadataUtil.buildMetadata(event.attributes, metadata.properties)
        metadata.properties.clear()
        metadata.properties.addAll(properties)
        return metadata
    }



    fun updateEventWithUser(identity: Identity) {
        tenantProvider.setTenat(identity.clientId.toString())
        eventRepository.updateEventsWithIdentityMatching(identity)

    }

    fun buildEvent(fromEvent: Event, request: HttpServletRequest): Event {
        with(fromEvent) {
            clientId = tenantProvider.tenant.toLong()
            ipAddress = request.ipAddr()
            timeZone = AuthenticationUtils.principal.timeZoneId
            agentString = request.getHeader("User-Agent")
        }
        return fromEvent
    }

}
