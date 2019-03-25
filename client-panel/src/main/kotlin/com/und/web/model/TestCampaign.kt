package com.und.web.model

import com.und.model.CampaignStatus
import com.und.model.jpa.CampaignType
import com.und.model.jpa.SmsTemplate
import java.time.LocalDateTime
import javax.validation.constraints.NotNull

class TestCampaign {
    var emailTemplate:EmailTemplate?=null
    var smsTemplate:SmsTemplate?=null
    var androidTemplate:AndroidTemplate?=null
    var webPushTemplate:WebPushTemplate?=null
//    var iosPushTemplate:IosPushTemplate
    var findByType:String?=null
    var toAddresses:String?=null
    @NotNull
    lateinit var campaignType: CampaignType
    var segmentationID: Long?=null
    var serviceProviderId:Long?=null
    var fromUser:String?=null
    var clientEmailSettingId:Long?=null
}

//{
//    "emailTemplate:EmailTemplate":{},
//    "findByType":"EMAIL",
//    "toAddresses":"",
//    "campaignType": "EMAIL",
//    "segmentationID":-2,
//    "serviceProviderId":1,
//    "fromUser":"",
//    "clientEmailSettingId":3
//}