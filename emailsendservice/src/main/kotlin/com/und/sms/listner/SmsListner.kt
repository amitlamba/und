package com.und.sms.listner

import com.und.model.utils.Sms
import com.und.sms.service.SmsService
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

    @StreamListener("smsEventReceive")
    fun sendSmsCampaign(sms: Sms) {
        smsService.sendSms(sms)
    }

    fun sendSms(){
        //check is there is any error or not for this campaign
        //get users
        //build sms for each user one by one.
        //if there is any error update campaign status to error and pause that campaign
        //update status of that group to delivered,error,undelivered.
    }
}

data class SmsTriggerInfo(val clientId:Long,
                          val campaignId:Long,
                          val segmentId:Long,
                          val executionId:Long,
                          val serviceProviderId:Long,
                          val templateId:Long,
                          val groupId:Long,
                          val groupStatus:GroupStatus)

enum class GroupStatus{
    DELIVERED,
    ERROR,
    UNDELIVERED
}