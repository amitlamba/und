package com.und.web.model

import javax.validation.constraints.NotNull

class AbCampaign {
    var id:Long?=null
    var campaignId:Long?=null
    @NotNull
    lateinit var campaign:Campaign
    @NotNull
    var variants:List<Variant> = emptyList()
    @NotNull
    var runType:RunType=RunType.AUTO
    var remind:Boolean=true
    var waitTime:Int?=null
    var sampleSize:Int?=null
}

class Variant{
    var id:Long?=null
    var campaignId:Long?=null
    @NotNull
    var percentage:Int?=null
    @NotNull
    var name:String?=null
    @NotNull
    var users:Int?=null
    var winner:Boolean=false
    @NotNull
    var templateId:Int?=null
}

enum class RunType{
    MANUAL,
    AUTO
}