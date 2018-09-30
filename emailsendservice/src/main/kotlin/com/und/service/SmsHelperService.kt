package com.und.service

import com.und.model.mongo.SmsStatus
import com.und.model.mongo.SmsStatusUpdate
import com.und.model.utils.Sms
import com.und.repository.jpa.SmsTemplateRepository
import com.und.repository.mongo.SmsSentRepository
import com.und.utils.TenantProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class SmsHelperService {

    @Autowired
    private lateinit var smsSentRepository: SmsSentRepository
    @Autowired
    private lateinit var smsTemplateRepository: SmsTemplateRepository

    fun updateBody(sms: Sms): Sms {

        val smsToSend = sms.copy()
        val smsTemplate = smsTemplateRepository.findByIdAndClientID(sms.smsTemplateId, sms.clientID)
        smsToSend.smsBody = smsTemplate?.smsTemplateBody
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
                smsStatus = status
        )
        TenantProvider().setTenant(smsToSend.clientID.toString())
        val mongoSmsPersisted: com.und.model.mongo.Sms? = smsSentRepository.save(mongoSms)

        return mongoSmsPersisted?.id
    }

    fun updateSmsStatus(mongoSmsId: String?, sent: SmsStatus, clientID: Long, message: String?, clickTrackEventId: String? = null) {
        TenantProvider().setTenant(clientID.toString())
        val mongoSms = mongoSmsId?.let { smsSentRepository.findById(mongoSmsId).get() }
        if (mongoSms != null && mongoSms.smsStatus.order < sent.order) {
            mongoSms.smsStatus = SmsStatus.READ
            mongoSms.statusUpdates.add(SmsStatusUpdate(LocalDateTime.now(ZoneId.of("UTC")), sent, clickTrackEventId, message))
            smsSentRepository.save(mongoSms)
        }

    }


}