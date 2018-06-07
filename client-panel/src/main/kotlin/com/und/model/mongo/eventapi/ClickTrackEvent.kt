package com.und.model.mongo.eventapi

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.*

@Document(collection = "#{tenantProvider.getTenant()}_click_event")
data class ClickTrackEvent(
        val name: String = "",
        @Id
        private var id: String? = null,
        var clientId: String = "-1",
        var instanceId: String? = null,
        val eventUser: EventUser = EventUser(),
        val geoDetails: GeoDetails = GeoDetails(),
        var systemDetails: SystemDetails? = null,
        val localDateTime: LocalDateTime = LocalDateTime.now(),
        val attributes: HashMap<String, Any> = hashMapOf(),
        var userIdentified:Boolean = false,
        var url: String,
        var emailUid: String
)