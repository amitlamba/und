package com.und.web.model

import java.io.Serializable
import java.time.LocalDateTime
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

class AndroidTemplate :Serializable{

    var id:Long?=null
    @NotNull
    lateinit var name:String
    @NotNull
//    @Size(min=8,max = 45)
    lateinit var title:String
    @NotNull
    lateinit var body:String
    var channelId:String?=null           //mandatory for api 28 sdk 26+
    var channelName:String?=null        //mandatory for api 28 sdk 26+
    @Pattern(regexp = "^http.{0,1}://.*$")
    var imageUrl:String?=null
    @Pattern(regexp = "^http.{0,1}://.*$")
    var largeIconUrl:String?=null
    var deepLink:String?=null
    var actionGroup:List<Action>?=null
    @Pattern(regexp = "^.*(.mp3)$")
    var sound:String?=null
    var badgeIcon=BadgeIconType.BADGE_ICON_NONE
    var collapse_key:String?=null
    var priority= Priority.NORMAL
    var timeToLive:Long?=null                //seconds
    @NotNull
    var fromUserNDot:Boolean=true
    var customKeyValuePair:HashMap<String,String>?=null
    var creationTime: LocalDateTime= LocalDateTime.now()

}

class Action :Serializable{
    var id:Long?=null
    @NotNull
    lateinit var actionId: String
    @NotNull
    lateinit var label: String
    var clientId:Long?=null
    var deepLink: String? = null
    var icon: String? = null
    var autoCancel: Boolean = true
    var creationTime:LocalDateTime= LocalDateTime.now()
}

enum class Priority(name:String){
    NORMAL("normal"),
    HIGH("high")
}

enum class BadgeIconType(name:String){
    BADGE_ICON_SMALL("small"),
    BADGE_ICON_NONE("none"),
    BADGE_ICON_LARGE("large")
}