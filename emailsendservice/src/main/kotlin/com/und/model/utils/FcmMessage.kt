package com.und.model.utils

import com.und.model.jpa.AndroidTemplate
import com.und.model.jpa.WebPushTemplate
import com.und.model.mongo.EventUser

data class FcmMessage(
        var clientId:Long,
        var templateId:Long,
        var to:String,
        var type:String,
        var campaignId:Long,
        var userId:String?=null,
        var eventUser:EventUser?=null,
        var data:MutableMap<String,Any> = mutableMapOf(),
        var serviceProviderId:Long?=null,
        var segmentId:Long?=null,
        var androidTemplate: AndroidTemplate?=null,
        var webPushTemplate: WebPushTemplate?=null
)