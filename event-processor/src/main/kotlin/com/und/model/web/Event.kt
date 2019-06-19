package com.und.model.web

import com.und.model.mongo.AppField
import java.util.*

open class Event {

    var id:String?=null
    lateinit var name: String

    var clientId: Long = -1L

    var creationTime:Long=System.currentTimeMillis()
    var identity: Identity = Identity()
    var ipAddress: String? = null

    var city: String? = null

    var state: String? = null

    var country: String? = null

    var latitude: String? = null

    var longitude: String? = null

    var agentString: String? = null
    var userIdentified: Boolean = false
    var lineItem: MutableList<LineItem> = mutableListOf()
    var attributes: HashMap<String, Any> = hashMapOf()

    var appField: AppField? = null

    //@get:JsonIgnore
    var timeZone: String? = null

    var notificationId: String? = null

}


class LineItem {
    var price: Int = 0
    var currency: String? = null
    var product: String? = null
    var categories: MutableList<String> = mutableListOf()
    var tags: MutableList<String> = mutableListOf()
    var quantity: Int = 0
    var properties: java.util.HashMap<String, Any> = hashMapOf()
}

data class Identity(
        //unique id assigned to a device, should always remain fixed, create new if not found
        var deviceId: String = "",
        //if userId is not found assign a new session id, handle change if user login changes, logouts etc
        var sessionId: String = "",
        // id of event user, this id is assigned when a user profile is identified.
        var userId: String? = null,
        var clientId: Int? = -1,
        var idf:Int = 0
)
data class EventMessage(
        var eventId: String = "",
        var clientId: Long,
        var userId: String?,
        var name: String,
        var creationTime: Date,
        var userIdentified:Boolean
)