package com.und.service

import com.und.common.utils.SmsServiceUtility
import com.und.model.mongo.SmsStatus
import com.und.model.utils.Sms
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service("testsmsservice")
class TestSmsService:CommonSmsService {
    companion object {
        val logger=LoggerFactory.getLogger(TestCampaignService::class.java)
    }

    @Autowired
    lateinit var templateContentCreationService: TemplateContentCreationService

    @Autowired
    lateinit var smsServiceUtility: SmsServiceUtility

    override fun sendSms(sms: Sms) {

        val model = sms.data
        sms.eventUser?.let { model["user"]}
        var smsToSend  = sms.copy()
        smsToSend.smsBody=templateContentCreationService.getTestSmsTemplateBody(sms.smsBody?:"",model)

        val response = smsServiceUtility.sendSmsWithoutTracking(smsToSend)
        if(response.status!=200) logger.info("Error in sending test campaign for client ${sms.clientID}")
    }
}