package com.und.eventapi.utils

import com.und.common.utils.DateUtils
import com.und.model.mongo.eventapi.*
import com.und.model.mongo.eventapi.SystemDetails
import com.und.web.model.eventapi.Event
import com.und.web.model.eventapi.EventUser
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.und.model.mongo.eventapi.Event as MongoEvent

fun Event.copyToMongo(): MongoEvent {
    val event = this
    val mongoEvent = MongoEvent(clientId = event.clientId, name = event.name)

    //copying system info
    val agentString = event.agentString
    if (agentString != null) {
        mongoEvent.agentString = agentString
        val sysDetail = systemDetails(agentString)
        val system = System()
        mongoEvent.system = system
        with(system) {

            os = SystemDetails(name = sysDetail.OS ?: "", version = "")
            if (sysDetail.browser != null && sysDetail.browserVersion != null) {
                browser = SystemDetails(sysDetail.browser!!, sysDetail.browserVersion!!)
            }
            application = SystemDetails(name = "", version = "")
            device = SystemDetails(name = sysDetail.deviceType ?: "", version = "")
        }
    }

    mongoEvent.geogrophy = Geogrophy(event.country, event.state, event.city)
    //TODO fix null values or empty strings not allowed
    mongoEvent.userId = event.identity.userId
    mongoEvent.sessionId = event.identity.sessionId
    mongoEvent.deviceId = event.identity.deviceId

    //copy geo details
    with(mongoEvent.geoDetails) {
        ip = event.ipAddress
        //FIXME find a way to update coordinates
        val latitude = if (event.latitude != null) event.latitude?.toFloat() else 0.0f
        val longitude = if (event.longitude != null) event.longitude?.toFloat() else 0.0f
        if ((latitude != null && longitude != null) && (latitude != 0.0f && longitude != 0.0f)) {
            geolocation = GeoLocation(coordinate = Coordinate(latitude = latitude, longitude = longitude))
        }

    }
    //FIXME hard coded charged
    if ("charged".equals(event.name, ignoreCase = false)) {
        mongoEvent.lineItem = event.lineItem
        mongoEvent.lineItem.forEach { item ->
            item.properties = toDateInMap(item.properties)

        }
    }
    //copy attributes
    mongoEvent.attributes.putAll(toDateInMap(event.attributes))
    return mongoEvent
}

private fun toDateInMap(attributes: HashMap<String, Any>): HashMap<String, Any> {
    val dateUtil = DateUtils()
    val outMap: HashMap<String, Any> = HashMap()
    attributes.forEach { key, value ->
        outMap += when (value) {
            is String -> (key to dateUtil.parseToDate(value))
            else -> (key to value)
        }
    }
    return outMap
}

fun com.und.model.mongo.eventapi.EventUser.copyNonNull(eventUser: EventUser): com.und.model.mongo.eventapi.EventUser {
    fun unchanged(new: String?, old: String?): String? = when {
        new == old -> old
        old == null -> new
        new == null -> old
        else -> new
    }

    val copyEventUser = com.und.model.mongo.eventapi.EventUser()

    copyEventUser.id = unchanged(eventUser.identity.userId, id)
    copyEventUser.additionalInfo.putAll(additionalInfo)
    copyEventUser.additionalInfo.putAll(eventUser.additionalInfo)
    copyEventUser.clientId = if (id == null) eventUser.clientId else clientId
    copyEventUser.creationTime = creationTime

    copyEventUser.identity = Identity()
    copyEventUser.identity.uid = unchanged(eventUser.uid, identity.uid)
    copyEventUser.identity.fbId = unchanged(eventUser.fbId, identity.fbId)
    copyEventUser.identity.googleId = unchanged(eventUser.googleId, identity.googleId)
    copyEventUser.identity.mobile = unchanged(eventUser.mobile, identity.mobile)
    copyEventUser.identity.email = unchanged(eventUser.email, identity.email)
    copyEventUser.identity.undId = unchanged(eventUser.undId, identity.undId)

    copyEventUser.standardInfo = StandardInfo()
    copyEventUser.standardInfo.firstname = unchanged(eventUser.firstName, standardInfo.firstname)
    copyEventUser.standardInfo.lastname = unchanged(eventUser.lastName, standardInfo.lastname)
    copyEventUser.standardInfo.gender = unchanged(eventUser.gender, standardInfo.gender)
    copyEventUser.standardInfo.dob = unchanged(eventUser.dob, standardInfo.dob)
    copyEventUser.standardInfo.country = unchanged(eventUser.country, standardInfo.country)
    copyEventUser.standardInfo.City = unchanged(eventUser.city, standardInfo.City)
    copyEventUser.standardInfo.Address = unchanged(eventUser.address, standardInfo.Address)
    copyEventUser.standardInfo.countryCode = unchanged(eventUser.countryCode, standardInfo.countryCode)
    return copyEventUser
}