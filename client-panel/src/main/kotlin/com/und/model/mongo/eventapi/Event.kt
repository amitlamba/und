package com.und.model.mongo.eventapi

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

/**
 * Created by shiv on 21/07/17.
 */

@Document(collection = "#{tenantProvider.getTenant()}_event")
class Event {
        @Id
        var id: String? = null
        lateinit var name: String
        var clientId: String = "-1"
        var identity: Identity = Identity()
        //var eventUser: EventUser = EventUser()
        var geoDetails: GeoDetails = GeoDetails()
        var systemDetails: SystemDetails = SystemDetails()
        var creationTime: LocalDateTime = LocalDateTime.now()
        var attributes: HashMap<String, Any> = hashMapOf()
        var userIdentified: Boolean = false
}

data class Identity(
        //unique id assigned to a device, should always remain fixed, create new if not found
        var deviceId: String? = null,
        //if userId is not found assign a new session id, handle change if user login changes, logouts etc
        var sessionId: String? = null,
        // id of event user, this id is assigned when a user profile is identified.
        var userId: String? = null
) {
        var eventUser: EventUser = EventUser()
}

data class GeoDetails(
        var ipAddress: String? = null,
        var city: String? = null,
        var state: String? = null,
        var country: String? = null,
        var latitude: String? = null,
        var longitude: String? = null
)


data class SystemDetails(
        var OS: String? = null,
        var browser: String? = null,
        var browserVersion: String? = null,
        var deviceType: String? = null, //mobile, tablet, laptop etc
        var agentString: String? = null
)

