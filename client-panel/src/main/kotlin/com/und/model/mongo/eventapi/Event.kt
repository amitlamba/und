package com.und.model.mongo.eventapi

import com.und.common.utils.DateUtils
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@TypeAlias("event")
//@Document(collection = "#{@tenantProvider.getTenant()}_event")
class Event(
        @field: Id var id: String? = null,
        val name: String,
        val clientId: Int,
        var lineItem: MutableList<LineItem> = mutableListOf(),
        var attributes: HashMap<String, Any> = hashMapOf(),
        var system: System = System(),
        var agentString: String? = null,
        var creationTime: Date = DateUtils.nowInUTC()
) {
    var geoDetails = GeoDetails()
    var deviceId: String = ""
    var userIdentified: Boolean = false
    var userId: String? = null
    var sessionId: String = ""

    var geogrophy: Geogrophy? = null

    var appfield: AppField? = null
}

class AppField {
    var appversion: String? = null
    var make: String? = null
    var model: String? = null
    var sdkversion: String? = null

}

data class Coordinate(val latitude: Float, val longitude: Float)
data class GeoLocation(val type: String = "Point", val coordinate: Coordinate)
class GeoDetails {
    var ip: String? = null
    var geolocation: GeoLocation? = null
}

class SystemDetails(val name: String, val version: String)
class System {
    var os: SystemDetails? = null
    var browser: SystemDetails? = null
    var device: SystemDetails? = null
    var application: SystemDetails? = null
}


class LineItem {
    var price: Int = 0
    var currency: String? = null
    var product: String? = null
    var categories: MutableList<String> = mutableListOf()
    var tags: MutableList<String> = mutableListOf()
    var quantity: Int = 0
    var properties: HashMap<String, Any> = hashMapOf()
}

class Geogrophy(val country: String?, val state: String?, val city: String?)