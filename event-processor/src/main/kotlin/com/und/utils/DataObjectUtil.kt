package com.und.utils

import com.und.model.Geogrophy
import com.und.model.MongoEvent
import com.und.model.SystemDetails
import com.und.model.web.Event
import java.time.Instant
import java.time.ZoneId
import java.util.*
import java.util.regex.Pattern
import com.und.model.System

fun Event.copyToMongo(): MongoEvent {
    val event = this
    val mongoEvent = MongoEvent(clientId = event.clientId, name = event.name)
    mongoEvent.timeZoneId = ZoneId.of(event.timeZone)

    mongoEvent.creationTime= Date.from(Instant.ofEpochMilli(event.creationTime).atZone(ZoneId.of("UTC")).toInstant())
    val agentString = event.agentString?:""
    var pattern = Pattern.compile("^(Mobile-Agent).*")
    var matcher=pattern.matcher(agentString)
    if (!matcher.matches() && agentString.isNotEmpty()) {
        mongoEvent.agentString = agentString
        val sysDetail = systemDetails(agentString)
        val system = System()
        mongoEvent.system = system
        with(system) {
            os = SystemDetails(name = sysDetail.OS ?: "", version = sysDetail.osVersion?:"")
            if (sysDetail.browser != null) {
                browser = SystemDetails(sysDetail.browser!!, sysDetail.browserVersion?:"")
            }
            application = SystemDetails(name = "", version = "")
            device = SystemDetails(name = sysDetail.deviceType ?: "", version = sysDetail.deviceVersion?:"")
        }
    }

    mongoEvent.geogrophy = Geogrophy(event.country, event.state, event.city)
    mongoEvent.appfield = event.appField
    return mongoEvent
}
