package com.und.web.model

import java.io.Serializable
import java.time.LocalDateTime
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

class AndroidTemplate :Serializable{

    var id:Long?=null
    @NotNull
    @Size(min=3,max = 50,message = "Length should be greater than 3 character")
    lateinit var name:String
    @NotNull
    @Size(min=5,max = 100,message = "Length should be greater than 5 character")
    lateinit var title:String
    @NotNull
    @Size(min=10,max = 512)
    lateinit var body:String
    @Size(min=3 ,message = "Length should be greater than 3 character")
    var channelId:String?=null
    @Size(min=3,message = "Length should be greater than 3 character")//mandatory for api 28 sdk 26+
    var channelName:String?=null        //mandatory for api 28 sdk 26+
    @Pattern(regexp="^$|^http[s]{0,1}://.+$",message = "Pattern must be in this format http://link or https://link")
    var imageUrl:String?=null
    @Pattern(regexp="^$|^http[s]{0,1}://.+$",message = "Pattern must be in this format http://link or https://link")
    var largeIconUrl:String?=null
    var deepLink:String?=null
    var actionGroup:List<Action>?=null
    @Pattern(regexp = "^$|^.*(.mp3)$",message = "File must be in mp3 format eg. beach_boy.mp3")
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
    var actionId: String?=null
    @NotNull
    @Size(min=3,message = "Length should be greater than 3 character")
    var label: String?=null
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