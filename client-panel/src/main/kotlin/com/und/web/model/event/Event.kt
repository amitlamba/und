package com.und.web.model.event

import com.und.model.mongo.eventapi.AppField
import com.und.model.mongo.eventapi.LineItem
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

open class Event {

    lateinit var name: String
    var clientId: Int = -1
    var creationTime: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))
    var ipAddress: String? = null
    var city: String? = null
    var state: String? = null
    var country: String? = null
    var latitude: Float? = null
    var longitude: Float? = null
    var agentString: String? = null
    var userIdentified: Boolean = false
    var lineItem: MutableList<LineItem> = mutableListOf()
    var attributes: HashMap<String, Any> = hashMapOf()
    var appfield: AppField? = null

}