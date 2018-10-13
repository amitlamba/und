package com.und.service

import com.und.model.jpa.SmsTemplate
import com.und.model.Status
import com.und.repository.jpa.SmsTemplateRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SmsTemplateService {

    @Autowired
    private lateinit var smsTemplateRepository: SmsTemplateRepository

    fun getDefaultSmsTemplates(): List<SmsTemplate> {
        return smsTemplateRepository.findByClientIDAndStatus()
    }

    fun getClientSmsTemplates(clientID: Long, smsTemplateID: Long?): List<SmsTemplate> {
        return if (smsTemplateID == null) {
            smsTemplateRepository.findByClientIDAndStatus(clientID)
        } else
            listOf(smsTemplateRepository.findByIdAndClientIDAndStatus(smsTemplateID, clientID))
    }

    fun saveSmsTemplate(smsTemplate: SmsTemplate): Long {
        smsTemplate.status = Status.ACTIVE
        val save = smsTemplateRepository.save(smsTemplate)
        return save.id!!
    }

    fun getUserEventAttributes() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}