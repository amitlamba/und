package com.und.service.eventapi

import com.und.common.utils.MetadataUtil
import com.und.eventapi.utils.copyToMongo
import com.und.eventapi.utils.ipAddr
import com.und.config.EventStream
import com.und.model.mongo.eventapi.EventMetadata
import com.und.model.mongo.eventapi.System
import com.und.model.mongo.eventapi.SystemDetails
import com.und.repository.mongo.EventMetadataRepository
import com.und.repository.mongo.EventRepository
import com.und.repository.mongo.EventUserRepository
import com.und.security.utils.AuthenticationUtils
import com.und.security.utils.TenantProvider
import com.und.web.exception.EventNotFoundException
import com.und.web.model.eventapi.Event
import com.und.web.model.eventapi.EventMessage
import com.und.web.model.eventapi.Identity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import java.util.regex.Pattern
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
    fun save(event: Event) {

        saveEvent(event)

    }

    fun saveEvent(event: Event): String? {
        val clientId = event.clientId
        tenantProvider.setTenat(clientId.toString())
        val mongoEvent = event.copyToMongo()
        mongoEvent.clientTime.hour
        var agentString=event.agentString
        var pattern = Pattern.compile("^(Mobile-Agent).*")
        var matcher=pattern.matcher(agentString)
        if(matcher.matches() && agentString!=null) {
            val system = System()
            mongoEvent.system = system
            var agent=agentString.split("/")
            with(system) {
                os = SystemDetails(name = agent[1], version = agent[2])
                browser = SystemDetails(name = agent[3], version = agent[4])
                device = SystemDetails(name = agent[5], version = agent[6])
                application = SystemDetails(name = agent[7], version = agent[8])
            }

            mongoEvent.system=system
        }
        val eventMetadata = buildMetadata(mongoEvent)
        eventMetadataRepository.save(eventMetadata)
        //FIXME add to metadata
        val saved = eventRepository.insert(mongoEvent)

        eventStream.outEventForLiveSegment().send(MessageBuilder.withPayload(buildEventForLiveSegment(saved)).build())
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
            var agent=request.getHeader("User-Agent")
            agentString = if(agent!="mobile") agent else request.getHeader("Mobile-Agent")
        }
        return fromEvent
    }

    fun buildEventForLiveSegment(fromEvent: com.und.model.mongo.eventapi.Event): EventMessage{
        val eventId = fromEvent.id
        if(eventId != null){
            return EventMessage(eventId, fromEvent.clientId, fromEvent.userId, fromEvent.name, fromEvent.creationTime)
        } else {
            throw EventNotFoundException("Event with null id")
        }

    }

}
