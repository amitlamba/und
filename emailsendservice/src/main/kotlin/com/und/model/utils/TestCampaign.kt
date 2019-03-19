package com.und.model.utils

import com.und.model.jpa.AndroidTemplate
import com.und.model.utils.Campaign
import com.und.model.jpa.SmsTemplate
import com.und.model.jpa.WebPushTemplate

class TestCampaign {
    lateinit var campaign: Campaign
    lateinit var type: CampaignType
    var clientId:Long?=null
    var emailTemplate: EmailTemplate?=null
    var smsTemplate: SmsTemplate?=null
    var webTemplate: WebPushTemplate?=null
    var androidTemplate: AndroidTemplate?=null
    var findByType:String?=null
    var toAddresses:Array<String>?=null
//    var iosTemplate:IosTemplate
}


enum class CampaignType {
    EMAIL,
    SMS,
    PUSH_ANDROID,
    PUSH_WEB,
    PUSH_IOS
}