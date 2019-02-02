package com.und.service.eventapi

import com.und.common.utils.MetadataUtil
import com.und.eventapi.utils.copyToMongo
import com.und.eventapi.utils.ipAddr
import com.und.config.EventStream
import com.und.model.mongo.eventapi.*
import com.und.repository.mongo.*
import com.und.security.utils.AuthenticationUtils
import com.und.security.utils.TenantProvider
import com.und.web.exception.EventNotFoundException
import com.und.web.model.eventapi.Event
import com.und.web.model.eventapi.EventMessage
import com.und.web.model.eventapi.Identity
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import java.util.regex.Pattern
import javax.servlet.http.HttpServletRequest
import com.und.model.mongo.eventapi.Event as MongoEvent

@Service
class EventService {


    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var tenantProvider: TenantProvider

    @Autowired
    private lateinit var eventRepository: EventRepository

    @Autowired
    private lateinit var ipLocationRepository: IpLocationRepository

    @Autowired
    private lateinit var eventMetadataRepository: EventMetadataRepository

    @Autowired
    private lateinit var userMetadataRepository: CommonMetadataRepository

    @Autowired
    private lateinit var eventUserRepository: EventUserRepository

    @Autowired
    private lateinit var eventStream: EventStream

    fun findByName(name: String): List<MongoEvent> = eventRepository.findByName(name)


    fun toKafka(event: Event): Boolean = eventStream.outEvent().send(MessageBuilder.withPayload(event).build())


    @StreamListener("inEvent")
//    @SendTo("outEvent")
    fun save(event: Event) {

        saveEvent(event)

    }

    fun saveEvent(event: Event): String? {
        val clientId = event.clientId
        tenantProvider.setTenat(clientId.toString())
        val mongoEvent = event.copyToMongo()
        mongoEvent.clientTime.hour
        var agentString = event.agentString?:""
        var pattern = Pattern.compile("^(Mobile-Agent).*")
        var matcher = pattern.matcher(agentString)
        //No need to check is empty
        if (matcher.matches() && agentString.isNotEmpty()) {
            val system = System()
            mongoEvent.system = system
            var agent = agentString.split("/")
            with(system) {
                os = SystemDetails(name = agent[1], version = agent[2])
                browser = SystemDetails(name = agent[3], version = agent[4])
                device = SystemDetails(name = agent[5], version = agent[6])
                application = SystemDetails(name = agent[7], version = agent[8])
            }

            mongoEvent.system = system

            var appFileds=AppField()
            with(appFileds){
                make=agent[9]
                model=agent[6]
                sdkversion=agent[10]
                appversion=agent[8]
                os=agent[1]
            }
            mongoEvent.appfield=appFileds

        }
        if (event.country == null || event.state == null || event.city == null) {

            var geogrophy = getGeography(event.ipAddress)
            geogrophy?.let { mongoEvent.geogrophy = geogrophy }
        }


        val eventMetadata = buildMetadata(mongoEvent)
        eventMetadataRepository.save(eventMetadata)
        val technographicsMetadata = buildTechnoGraphics(mongoEvent)
        userMetadataRepository.updateTechnographics(clientId, technographicsMetadata)
        val appFieldsMetadata = buildAppFields(mongoEvent)
        userMetadataRepository.updateAppFields(clientId, appFieldsMetadata)
        //FIXME add to metadata
//        val saved = eventRepository.insert(mongoEvent)
        val id=ObjectId()
        mongoEvent.id=id.toString()
        eventRepository.save(mongoEvent)
        val saved=eventRepository.findById(id.toString()).get()
        eventStream.outEventForLiveSegment().send(MessageBuilder.withPayload(buildEventForLiveSegment(saved)).build())
        return saved.id
    }

    private inline fun addProperty(propertyName: String, optionName: String?, technographics: CommonMetadata) {
        optionName?.let { option ->
            val property = Property()
            property.name = propertyName
            property.options.add(option)
            technographics.properties.add(property)
        }
    }

    private fun buildTechnoGraphics(event: MongoEvent): CommonMetadata {

        val technographics = CommonMetadata()
        technographics.name = "Technographics"
        addProperty("browser", event.system.browser?.name, technographics)
        addProperty("os", event.system.os?.name, technographics)
        addProperty("device", event.system.device?.name, technographics)

        return technographics
    }

    private fun buildAppFields(event: MongoEvent): CommonMetadata {

        val appFields = CommonMetadata()
        appFields.name = "AppFields"
        addProperty("appversion", event.appfield?.appversion, appFields)
        addProperty("make", event.appfield?.make, appFields)
        addProperty("model", event.appfield?.model, appFields)
        addProperty("os", event.appfield?.os, appFields)
        addProperty("sdkversion", event.appfield?.sdkversion, appFields)

        return appFields
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
            if(fromEvent.clientId!=-1L) clientId =fromEvent.clientId else clientId =tenantProvider.tenant.toLong()
            ipAddress = request.ipAddr()
            timeZone = AuthenticationUtils.principal.timeZoneId
            var agent = request.getHeader("User-Agent")
            agentString = if (agent != "mobile") agent else request.getHeader("Mobile-Agent")
        }
        return fromEvent
    }

    fun buildEventForLiveSegment(fromEvent: com.und.model.mongo.eventapi.Event): EventMessage {
        val eventId = fromEvent.id
        if (eventId != null) {
            return EventMessage(eventId, fromEvent.clientId, fromEvent.userId, fromEvent.name, fromEvent.creationTime)
        } else {
            throw EventNotFoundException("Event with null id")
        }

    }

    private fun getGeography(ipAddress: String?): Geogrophy? {
        if (ipAddress != null) {
            return ipLocationRepository.getGeographyByIpAddress(ipAddress)
        }
        return null
    }

}
