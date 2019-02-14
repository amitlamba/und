package com.und.service

import com.und.model.mongo.EventUser
import com.und.model.mongo.SmsStatus
import com.und.model.utils.Sms
import com.und.repository.jpa.SmsTemplateRepository
import com.und.repository.mongo.SmsSentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SmsHelperService {

    @Autowired
    private lateinit var smsSentRepository: SmsSentRepository
    @Autowired
    private lateinit var smsTemplateRepository: SmsTemplateRepository
    @Autowired
    private lateinit var templateContentCreationService: TemplateContentCreationService


    fun updateBody(sms: Sms): Sms {

        val smsToSend = sms.copy()
        val smsTemplate = smsTemplateRepository.findByIdAndClientID(sms.smsTemplateId, sms.clientID)
        val model=sms.data
        sms.eventUser?.let {
            model["user"] = it
        }
        smsToSend.smsBody = smsTemplate?.let { templateContentCreationService.getSmsBody(smsTemplate,model)}
        return smsToSend
    }

    fun saveSmsInMongo(smsToSend: Sms, status: SmsStatus, mongoEmailId: String? = null): String? {
        val mongoSms: com.und.model.mongo.Sms = com.und.model.mongo.Sms(
                id = mongoEmailId,
                clientID = smsToSend.clientID,
                fromSmsAddress = smsToSend.fromSmsAddress,
                toSmsAddresses = smsToSend.toSmsAddresses,
                smsBody = smsToSend.smsBody,
                smsTemplateId = smsToSend.smsTemplateId,
                userID = smsToSend.eventUser?.id,
                status = status,
                campaignId = smsToSend.campaignId,
                segmentId = smsToSend.segmentId
        )
        //TenantProvider().setTenant(smsToSend.clientID.toString())
        smsSentRepository.saveSms(mongoSms, smsToSend.clientID )

        return mongoEmailId
    }

    fun updateSmsStatus(mongoSmsId: String, sent: SmsStatus, clientID: Long, message: String?, clickTrackEventId: String? = null) {
//        TenantProvider().setTenant(clientID.toString())
//        val mongoSms = mongoSmsId?.let { smsSentRepository.findById(mongoSmsId).get() }
//        if (mongoSms != null && mongoSms.smsStatus.order < sent.order) {
//            mongoSms.smsStatus = SmsStatus.READ
//            mongoSms.statusUpdates.add(SmsStatusUpdate(LocalDateTime.now(ZoneId.of("UTC")), sent, clickTrackEventId, message))
//            smsSentRepository.updateStatus(mongoSmsId, SmsStatus.READ, clientID, null, "")
//        }
        smsSentRepository.updateStatus(mongoSmsId, sent, clientID, null, message?:"")

    }


}