package com.und.web.model

class AbCampaign {
    var id:Long?=null
    var campaignId:Long?=null
    lateinit var campaign:Campaign
    var variants:List<Variant> = emptyList()
    var runType:RunType=RunType.AUTO
    var remind:Boolean=true
    var waitTime:Int?=null
    var sampleSize:Int?=null
    var liveSampleSize:Int?=null
}

class Variant{
    var id:Long?=null
    var campaignId:Long?=null
    var percentage:Int?=null
    var name:String?=null
    var users:Int?=null
    var winner:Boolean=false
    var templateId:Int?=null
}

enum class RunType{
    MANUAL,
    AUTO
}