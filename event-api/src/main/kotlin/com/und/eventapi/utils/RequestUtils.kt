package com.und.eventapi.utils

import com.und.web.model.eventapi.Event
import com.und.web.model.eventapi.EventUser
import com.und.model.mongo.eventapi.EventUser as MongoEventUser
import com.und.model.mongo.eventapi.Event as MongoEvent
import com.und.model.mongo.eventapi.Identity
import com.und.model.mongo.eventapi.StandardInfo
import com.und.model.mongo.eventapi.System
import eu.bitwalker.useragentutils.UserAgent
import javax.servlet.http.HttpServletRequest

//TODO write test cases for this class

fun HttpServletRequest.ipAddr(): String {


    fun ipExistsInHeader(header: String): Boolean {
        val ip = this.getHeader(header)
        return !(ip.isNullOrEmpty() || "unknown".equals(ip, ignoreCase = true))
    }

    val ip = when {
        ipExistsInHeader("X-Forwarded-For") -> this.getHeader("X-Forwarded-For")
        ipExistsInHeader("Proxy-Client-IP") -> this.getHeader("Proxy-Client-IP")
        ipExistsInHeader("WL-Proxy-Client-IP") -> this.getHeader("WL-Proxy-Client-IP")
        ipExistsInHeader("HTTP_CLIENT_IP") -> this.getHeader("HTTP_CLIENT_IP")
        ipExistsInHeader("HTTP_X_FORWARDED_FOR") -> this.getHeader("HTTP_X_FORWARDED_FOR")
        else -> this.remoteAddr
    }
    return ip
}


fun systemDetails(agentString: String): SystemDetails {
    val systemDetails = SystemDetails()
    val userAgent = UserAgent.parseUserAgentString(agentString)
    val browser = userAgent.browser

    val operatingSystem = userAgent.operatingSystem
    val deviceType = operatingSystem.deviceType
    val osName = operatingSystem.getName()
    systemDetails.browser = browser.getName()
    systemDetails.browserVersion = userAgent.browserVersion?.version
    systemDetails.OS = osName
    systemDetails.deviceType = deviceType.getName()

    return systemDetails
}

data class SystemDetails(
        var OS: String? = null,
        var browser: String? = null,
        var browserVersion: String? = null,
        var deviceType: String? = null, //mobile, tablet, laptop etc
        var agentString: String? = null
)





