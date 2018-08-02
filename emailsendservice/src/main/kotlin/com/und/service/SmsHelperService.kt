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
    private lateinit var smsSentRepository:SmsSentRepository
    @Autowired
    private lateinit var smsTemplateRepository: SmsTemplateRepository

    fun updateBody(sms: Sms): Sms {

        var smsToSend=sms.copy()
        var smsTemplate=smsTemplateRepository.findByIdAndClientID(sms.smsTemplateId,sms.clientID)
        smsToSend.smsBody=smsTemplate?.smsTemplateBody
        return smsToSend
    }

    fun saveSmsInMongo(smsToSend: Sms, noT_SENT: SmsStatus): String? {
        var mongoSms:com.und.model.mongo.Sms =com.und.model.mongo.Sms(
                smsToSend.clientID,
                smsToSend.fromSmsAddress,
                smsToSend.toSmsAddresses,
                smsToSend.smsBody,
                smsToSend.smsTemplateId,
                smsToSend.eventUser?.id,
                smsStatus = noT_SENT
        )
        TenantProvider().setTenant(smsToSend.clientID.toString())
        val mmongoSmsPersisted: com.und.model.mongo.Sms? = smsSentRepository.save(mongoSms)

            return mmongoSmsPersisted?.id
    }

    fun updateSmsStatus(mongoSmsId: String?, sent: SmsStatus, clientID: Long,clickTrackEventId: String? = null) {
        TenantProvider().setTenant(clientID.toString())
        var mongoSms=smsSentRepository.findById(mongoSmsId).get()
        if(mongoSms.smsStatus.order<sent.order){
            mongoSms.smsStatus=SmsStatus.READ
            mongoSms.statusUpdates.add(SmsStatusUpdate(LocalDateTime.now(ZoneId.of("UTC")),sent,clickTrackEventId))
            smsSentRepository.save(mongoSms)
        }

    }


}