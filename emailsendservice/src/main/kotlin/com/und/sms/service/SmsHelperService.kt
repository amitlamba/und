package com.und.sms.service

import com.und.common.utils.ReplaceNullPropertyOfEventUser
import com.und.model.mongo.SmsStatus
import com.und.model.utils.Sms
import com.und.service.TemplateContentCreationService
import com.und.sms.repository.jpa.SmsTemplateRepository
import com.und.sms.repository.mongo.SmsSentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.util.regex.Pattern

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
        val variable = getSmsTemplateVariable(sms)
        val user = ReplaceNullPropertyOfEventUser.replaceNullPropertyOfEventUser(sms.eventUser,variable)
        val model=sms.data
        user?.let {
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

    @Cacheable(value = ["templateVariable"],key = "'sms_template_variable'+#email.clientID+'_'+#email.smsTemplateId" )
    fun getSmsTemplateVariable(email: Sms):Set<String>{
        val listOfVariable = mutableSetOf<String>()

        val template=smsTemplateRepository.findByIdAndClientID(email.smsTemplateId,email.clientID)
        template?.let{
            val tem=it

            val body=tem.smsTemplateBody
            val regex="(\\$\\{.*?\\})"
            val pattern = Pattern.compile(regex)

            val bodyMatcher = pattern.matcher(body)
            var i=0
            while (bodyMatcher.find()){
                listOfVariable.add(bodyMatcher.group(i+1))
            }

        }

        return listOfVariable
    }

}