package com.und.kafkalisterner

import com.und.model.utils.Sms
import com.und.service.SmsService
import com.und.utils.loggerFor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.stereotype.Service

@Service
class SmsListner {
    companion object {
        val logger = loggerFor(SmsListner::class.java)
    }

    @Autowired
    private lateinit var smsService: SmsService

    @StreamListener("smsEventSend")
    fun sendSmsCampaign(sms: Sms) {
        smsService.sendSms(sms)
    }
}