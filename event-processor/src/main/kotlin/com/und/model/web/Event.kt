package com.und.model.web

import com.und.model.AppField

open class Event {

    lateinit var name: String

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