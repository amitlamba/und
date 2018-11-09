package com.und.model.mongo

import java.time.LocalDateTime


class Message(clientId:Long,campaignId:Long){
    lateinit var message:FcmMessage
    lateinit var creationTime:LocalDateTime
}
class FcmMessage {
    lateinit var to:String
    var notification:CommonNotification?=null
//    var data:HashMap<String,String>?=null
    var android:AndroidConfig?=null
//    var apn:ApnConfig?=null
//    var webpush:WebPushConfig?=null
}

class CommonNotification{
    lateinit var title:String
    lateinit var body:String
}

class AndroidConfig{
    var collapse_key:String?=null
    var ttl:Int?=null               //default is 28
    var priority:Priority=Priority.NORMAL
    var data:HashMap<String,String>?=null
}

enum class Priority(name:String){
    NORMAL("normal"),
    HIGH("high")
}

