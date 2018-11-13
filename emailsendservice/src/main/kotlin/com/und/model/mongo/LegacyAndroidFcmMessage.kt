package com.und.model.mongo

class LegacyFcmMessage {
    lateinit var to:String
    lateinit var data:HashMap<String,String>
    var collapse_key:String?=null
    var time_to_live:Long?=null
    var priority:Priority=Priority.NORMAL
}