package com.und.service

import com.und.model.jpa.EmailTemplate
import com.und.model.jpa.Template
import com.und.repository.jpa.ClientSettingsEmailRepository
import com.und.repository.jpa.EmailTemplateRepository
import com.und.security.utils.AuthenticationUtils
import com.und.web.controller.exception.EmailTemplateDuplicateNameException
import com.und.web.controller.exception.EmailTemplateNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import com.und.web.model.EmailTemplate as WebEmailTemplate

@Service
class EmailTemplateService {

    @Autowired
    private lateinit var emailTemplateRepository: EmailTemplateRepository

    @Autowired
    private lateinit var clientSettingsEmailRepository: ClientSettingsEmailRepository


    fun getDefaultEmailTemplates(): List<WebEmailTemplate> {
        val emailTemplates = emailTemplateRepository.findByClientID()
        return emailTemplates.map { buildWebEmailTemplate(it) }
    }

    fun getClientEmailTemplates(emailTemplateId: Long?): List<WebEmailTemplate> {
        return if (emailTemplateId == null) {
            getClientEmailTemplates()
        } else {
            getEmailTemplate(emailTemplateId)
        }

    }

    @Cacheable(cacheNames = ["emailtemplate"],key = "'client_'+T(com.und.security.utils.AuthenticationUtils).INSTANCE.getClientID()+'_template_'+#emailTemplateId" )
    fun getEmailTemplate(emailTemplateId: Long): List<WebEmailTemplate> {
        val clientId = AuthenticationUtils.clientID
        println(clientId)
        return clientId?.let {
            val emailTemplateOtion = emailTemplateRepository.findByIdAndClientID(emailTemplateId, clientId)
            if (emailTemplateOtion.isPresent) listOf(buildWebEmailTemplate(emailTemplateOtion.get())) else emptyList()
        } ?: emptyList()
    }

    @Cacheable(cacheNames = ["emailtemplate"],key = "'client_'+T(com.und.security.utils.AuthenticationUtils).INSTANCE.getClientID()+'_template_list'" )
    fun getClientEmailTemplates(): List<WebEmailTemplate> {
        val clientId = AuthenticationUtils.clientID
        return clientId?.let { emailTemplateRepository.findByClientID(clientId).map { buildWebEmailTemplate(it) } }
                ?: emptyList()
    }

    @CachePut(cacheNames = ["emailtemplate"], key = "'client_'+T(com.und.security.utils.AuthenticationUtils).INSTANCE.getClientID()+'_template_'+#webEmailTemplate.id" )
    //@CacheEvict(cacheNames = ["emailtemplate"],key = "'client_'+T(com.und.security.utils.AuthenticationUtils).INSTANCE.getClientID()+'_template_list'" )
    fun saveEmailTemplate(webEmailTemplate: WebEmailTemplate): Long {
        val clientId = AuthenticationUtils.clientID
        if (clientId != null) {
            val existingTemplate = emailTemplateRepository.findByNameAndClientID(webEmailTemplate.name, clientId)
            val nameExists = existingTemplate.isPresent && existingTemplate.get().id != webEmailTemplate.id
            if (nameExists) {
                throw EmailTemplateDuplicateNameException("Template with name : ${webEmailTemplate.name} already exists")
            }
        }
        val emailTemplate = buildEmailTemplate(webEmailTemplate)

        val persistedemailTemplate = emailTemplateRepository.save(emailTemplate)
        return persistedemailTemplate.id ?: -1
    }

    fun getEmailTemplateById(id: Long): WebEmailTemplate {
           val emailTemplate = getEmailTemplate(id)
            if(emailTemplate.isNotEmpty()){
                return emailTemplate.first()
            }
            else throw EmailTemplateNotFoundException("Email Template with id $id not found")
        }

    fun getUserEventAttributes() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun buildWebEmailTemplate(emailTemplate: EmailTemplate): WebEmailTemplate {
        val webTemplate = WebEmailTemplate()
        webTemplate.id = emailTemplate.id
        webTemplate.name = emailTemplate.name
        webTemplate.emailTemplateBody = emailTemplate.emailTemplateBody?.template ?: ""
        webTemplate.emailTemplateSubject = emailTemplate.emailTemplateSubject?.template ?: ""
        webTemplate.from = emailTemplate.from
        webTemplate.messageType = emailTemplate.messageType
        webTemplate.parentID = emailTemplate.parentID
        webTemplate.tags = emailTemplate.tags
        webTemplate.editorSelected = emailTemplate.editorSelected
        webTemplate.dateCreated = emailTemplate.dateCreated
        return webTemplate

    }

    private fun buildEmailTemplate(webTemplate: WebEmailTemplate): EmailTemplate {
        val emailTemplate = EmailTemplate()
        emailTemplate.clientID = AuthenticationUtils.clientID
        emailTemplate.appuserID = AuthenticationUtils.principal.id
        emailTemplate.id = webTemplate.id
        emailTemplate.parentID = webTemplate.parentID
        emailTemplate.name = webTemplate.name
        emailTemplate.from = webTemplate.from
        emailTemplate.messageType = webTemplate.messageType
        emailTemplate.tags = webTemplate.tags
        emailTemplate.editorSelected = webTemplate.editorSelected



        emailTemplate.emailTemplateSubject = buildTemplate(
                emailTemplate,
                webTemplate.emailTemplateSubject,
                "${emailTemplate.clientID}:${emailTemplate.name}:subject")

        emailTemplate.emailTemplateBody = buildTemplate(
                emailTemplate,
                webTemplate.emailTemplateBody,
                "${emailTemplate.clientID}:${emailTemplate.name}:body")

        return emailTemplate

    }

    private fun buildTemplate(emailTemplate: EmailTemplate, templateText: String, name: String): Template {
        val template = Template()
        template.appuserID = emailTemplate.appuserID
        template.clientID = emailTemplate.clientID
        template.template = templateText
        template.name = name
        return template
    }

    fun checkFromUserExistOrNot(clientId: Long, from: String): Boolean {
        return clientSettingsEmailRepository.existsByClientIdAndEmailAndVerified(clientId,from,true)
    }
}