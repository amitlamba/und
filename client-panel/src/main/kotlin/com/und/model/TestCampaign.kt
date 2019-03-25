package com.und.model


import com.und.model.jpa.AndroidTemplate
import com.und.model.jpa.CampaignType
import com.und.model.jpa.SmsTemplate
import com.und.model.jpa.WebPushTemplate

import com.und.web.model.Campaign
import com.und.web.model.EmailTemplate

class TestCampaign {
    lateinit var campaign: Campaign //web //fromuser ,clientid,segmentid,serviceproviderid
    lateinit var type: CampaignType
    var clientId:Long?=null
    var emailTemplate: EmailTemplate?=null //new subject and body   web
    var smsTemplate: SmsTemplate?=null  //jpa
    var webTemplate: WebPushTemplate?=null //jpa
    var androidTemplate: AndroidTemplate?=null  //jpa
    var findByType:String?=null
    var toAddresses:Array<String>?=null
//    var iosTemplate:IosTemplate
}