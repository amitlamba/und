package com.und.service

import com.und.model.jpa.SmsTemplate
import com.und.model.Status
import com.und.repository.jpa.SmsTemplateRepository
import com.und.security.utils.AuthenticationUtils
import com.und.web.controller.exception.CustomException
import org.hibernate.exception.ConstraintViolationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
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
        try{
            val save = smsTemplateRepository.save(smsTemplate)
            return save.id!!
        }catch (ex: ConstraintViolationException){
            throw CustomException("Template with this name already exists.")
        }catch (ex: DataIntegrityViolationException){
            throw CustomException("Template with this name already exists.")
        }
    }

    fun getUserEventAttributes() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun getSmsTemplateById(id:Long):SmsTemplate{
        var clientID=AuthenticationUtils.clientID?:throw AccessDeniedException("")
        var template=smsTemplateRepository.findByIdAndClientID(id,clientID)
        if(template.isPresent) return template.get()
        return throw CustomException("No template with id ${id} exists")
    }
}