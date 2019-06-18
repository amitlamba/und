package com.und.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.und.utils.DateUtils
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class MongoEvent(
        var id: String? = null,
        val name: String,
        val clientId: Long,
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

    var firstTime = false

    var timeZoneId = ZoneId.of("UTC")

    var clientTime = ClientTimeNow(LocalDateTime.now(timeZoneId))
    var notificationId: String? = null

    var appfield: AppField? = null
}

class ClientTimeNow(val time: LocalDateTime) {


    var hour: Int = time.hour
    var minute: Int = time.minute
    var second: Int = time.second
    var month: Int = time.monthValue
    var dayOfMonth: Int = time.dayOfMonth
    var dayOfWeek: Int = time.dayOfWeek.value
    var year: Int = time.year


}

class AppField {
    var appversion: String? = null
    var make: String? = null
    var model: String? = null
    var sdkversion: String? = null
    var os:String? = null

}

data class Coordinate(val latitude: Double, val longitude: Double)
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