package com.und.model.utils.eventapi

import java.util.HashMap

class Event {

    lateinit var name: String
    var identity=Identity()
    var clientId: Long = -1L
    var creationTime:Long=System.currentTimeMillis()

    var ipAddress: String? = null
    var city: String? = null

    var state: String? = null

    var country: String? = null

    var latitude: String? = null

    var longitude: String? = null

    var agentString: String? = null
    var userIdentified: Boolean = false
    var attributes: HashMap<String, Any> = hashMapOf()

    var timeZone:String? = null

    var notificationId:String? = null

}


data class Identity(
        //unique id assigned to a device, should always remain fixed, create new if not found
        var deviceId: String = "",
        //if userId is not found assign a new session id, handle change if user login changes, logouts etc
        var sessionId: String = "",
        // id of event user, this id is assigned when a user profile is identified.
        var userId: String? = null,
        var clientId: Int? = -1,
        var idf: Int =0
)