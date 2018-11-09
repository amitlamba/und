package com.und.web.model

import java.io.Serializable
import java.time.LocalDateTime
import javax.validation.constraints.NotNull

class WebPushTemplate :Serializable{

    var id:Long?=null
    @NotNull
    lateinit var title: String
    @NotNull
    lateinit var body: String
    @NotNull
    lateinit var name:String
    var lang: String? = null
    var badgeUrl: String? = null      //url of badge icon
    var iconUrl: String? = null       //url if icon
    var imageUrl: String? = null      //url of image in notification
    var tag: String? = null             //used to group notification
    var requireInteraction: Boolean = false
    var actionGroup: List<WebAction>? = null
    var urgency: String? = null
    var ttl: Long? = null
    var link: String? = null
    var customDataPair: HashMap<String, String>? = null
    var creationTime = LocalDateTime.now()
    var modifiedTime = LocalDateTime.now()
    var fromUserndot: Boolean = true
}

class WebAction :Serializable{
    var id:Long?=null
    var action: String? = null  //action id unique used to determine which action is clicked
    @NotNull
    lateinit var title: String
    var iconUrl: String? = null //url of icon
    var creationTime = LocalDateTime.now()
    var modifiedTime = LocalDateTime.now()
}