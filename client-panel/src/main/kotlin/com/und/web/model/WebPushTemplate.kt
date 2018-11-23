package com.und.web.model

import java.io.Serializable
import java.time.LocalDateTime
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

class WebPushTemplate :Serializable{

    var id:Long?=null
    @NotNull
    @Size(min=5,message = "Length should be greater than 5 character")
    lateinit var title: String
    @NotNull
    @Size(min=5,message = "Length should be greater than 5 character")
    lateinit var body: String
    @NotNull
    @Size(min=5,message = "Length should be greater than 5 character")
    lateinit var name:String
    var lang: String? = null
    @Pattern(regexp="^http.{0,1}://.+$",message = "Pattern must be in this format http://link or https://link")
    var badgeUrl: String? = null      //url of badge icon
    @Pattern(regexp="^http.{0,1}://.+$",message = "Pattern must be in this format http://link or https://link")
    var iconUrl: String? = null       //url if icon
    @Pattern(regexp="^http.{0,1}://.+$",message = "Pattern must be in this format http://link or https://link")
    var imageUrl: String? = null      //url of image in notification
    var tag: String? = null             //used to group notification
    var requireInteraction: Boolean = false
    var actionGroup: List<WebAction>? = null
    var urgency: String? = null
    var ttl: Long? = null
    var link: String? = null
    var collapse_key:String?=null
    var customDataPair: HashMap<String, String>? = null
    var creationTime = LocalDateTime.now()
    var modifiedTime = LocalDateTime.now()
    var fromUserndot: Boolean = true
}

class WebAction :Serializable{
    var id:Long?=null
    @NotNull
    lateinit var action: String  //action id unique used to determine which action is clicked
    @NotNull
    @Size(min=5,message = "Length should be greater than 5 character")
    lateinit var title: String
    @Pattern(regexp="^http.{0,1}://.+$",message = "Pattern must be in this format http://link or https://link")
    var iconUrl: String? = null //url of icon
    var creationTime = LocalDateTime.now()
    var modifiedTime = LocalDateTime.now()
}