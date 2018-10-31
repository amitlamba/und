package com.und.model.mongo

class FcmMessage {
    var to:String?=null
    var notifiation:CommonNotification?=null
    var data:HashMap<String,String>?=null
    var android:AndroidConfig?=null
//    var apn:ApnConfig?=null
//    var webpush:WebPushConfig?=null
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

enum class Priority{
    NORMAL,
    HIGH
}