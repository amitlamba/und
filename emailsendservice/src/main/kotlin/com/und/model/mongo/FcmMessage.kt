package com.und.model.mongo

class FcmMessage {
    var to:String?=null
    var notifiation:CommonNotification?=null
    var data:HashMap<String,String>?=null
    var android:AndroidConfig?=null
//    var apn:ApnConfig?=null
    var webpush:WebPushConfig?=null
}
class CommonNotification{
    var title:String?=null
    var body:String?=null
}
class AndroidConfig{
    var collapse_key:String?=null
    var ttl:Long?=null
    var data:HashMap<String,String>?=null
    var notification:AndroidNotification?=null
    var priority:Priority=Priority.NORMAL  //enum High Normal
}

class AndroidNotification{
    var title:String?=null
    var body:String?=null
    var sound:String?=null //  /res/raw/1.mp3 //optional
    var color:String?=null //  #ffffff   //optional
    var icon:String?=null  //  drawable/pic1 //optional
    var tag:String?=null   //optional
    var click_action:String?=null

    var body_loc_key:String?=null
    var body_loc_args:List<String>?=null

}

class WebPushConfig{
    var headers:WebPushHeaders?=null
    var notification:WebPushNotification?=null
    var data:HashMap<String,String>?=null
    var fcm_options:WebPushFcmOptions?=null
}

class WebPushHeaders{
    var Urgency:UrgencyOption=UrgencyOption.normal
    var TTL:Long?=28
}
enum class UrgencyOption(name:String){
    low("low"),
    normal("normal"),
    high("high")
}
class WebPushNotification{
    var title:String?=null
    var body:String?=null
    var badge:String?=null
    var lang:String?=null
    var icon:String?=null
    var tag:String?=null
    var image:String?=null
    var requireInteraction:Boolean=true
    var actions:List<WebPushNotificationAction>?=null
}
class WebPushNotificationAction{
    lateinit var action:String
    lateinit var title:String
    var icon:String?=null
}
class WebPushFcmOptions{
    var link:String?=null
}
enum class Priority{
    NORMAL,
    HIGH
}