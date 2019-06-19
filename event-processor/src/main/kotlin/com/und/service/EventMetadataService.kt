package com.und.service

import com.und.model.mongo.eventapi.CommonMetadata
import com.und.model.mongo.eventapi.EventMetadata
import com.und.model.mongo.eventapi.Property
import com.und.model.web.Event
import com.und.model.mongo.Event as MongoEvent
import com.und.repository.mongo.CommonMetadataRepository
import com.und.repository.mongo.EventMetadataRepository
import com.und.utils.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.util.*

@Service
class EventMetadataService {

    @Autowired
    private lateinit var eventMetadataRepository : EventMetadataRepository

    @Autowired
    private lateinit var userMetadataRepository: CommonMetadataRepository

    @Autowired
    private lateinit var mongoEventUtils: MongoEventUtils

    @StreamListener(Constants.BUILD_METADATA)
    fun buildEventMetadata(event: Event){
        var mongoEvent = com.und.model.mongo.Event(clientId = event.clientId, name = event.name)
        mongoEvent.timeZoneId = ZoneId.of(event.timeZone)
        mongoEvent.creationTime= Date.from(Instant.ofEpochMilli(event.creationTime).atZone(ZoneId.of("UTC")).toInstant())
        mongoEvent = mongoEvent.parseUserAgentString(event.agentString)
        if ("charged".equals(event.name, ignoreCase = false)) {
            mongoEvent.lineItem = event.lineItem
            mongoEvent.lineItem.forEach { item ->
                item.properties = mongoEventUtils.toDateInMap(item.properties)

            }
        }
        mongoEvent.attributes.putAll(mongoEventUtils.toDateInMap(event.attributes))

        val eventMetadata = buildMetadata(mongoEvent)
        eventMetadata.clientId = event.clientId
        eventMetadataRepository.save(eventMetadata,event.clientId)

        val technographicsMetadata = buildTechnoGraphics(mongoEvent)
        technographicsMetadata.clientId = event.clientId
        userMetadataRepository.updateTechnographics(event.clientId, technographicsMetadata)
        val appFieldsMetadata = buildAppFields(mongoEvent)
        appFieldsMetadata.clientId = event.clientId
        userMetadataRepository.updateAppFields(event.clientId, appFieldsMetadata)
    }

    private fun buildMetadata(event: MongoEvent): EventMetadata {
        val metadata = eventMetadataRepository.findByName(event.name,event.clientId) ?: EventMetadata()
        metadata.name = event.name
        val properties = MetadataUtil.buildMetadata(event.attributes, metadata.properties)
        metadata.properties.clear()
        metadata.properties.addAll(properties)
        return metadata
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
    private  fun addProperty(propertyName: String, optionName: String?, technographics: CommonMetadata) {
        optionName?.let { option ->
            val property = Property()
            property.name = propertyName
            property.options.add(option)
            technographics.properties.add(property)
        }
    }
}