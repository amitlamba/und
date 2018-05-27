package com.und.service

import com.und.exception.UndBusinessValidationException
import com.und.model.jpa.EmailTemplate
import com.und.model.jpa.Template
import com.und.web.model.EmailTemplate as WebEmailTemplate
import com.und.repository.jpa.EmailTemplateRepository
import com.und.repository.jpa.TemplateRepository
import com.und.security.utils.AuthenticationUtils
import com.und.web.model.ValidationError
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class EmailTemplateService {

    @Autowired
    private lateinit var emailTemplateRepository: EmailTemplateRepository
    @Autowired
    private lateinit var templateRepository: TemplateRepository

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

    private fun getEmailTemplate(emailTemplateId: Long): List<WebEmailTemplate> {
        val clientId = AuthenticationUtils.clientID
        return clientId?.let {
            listOf(emailTemplateRepository.findByIdAndClientID(emailTemplateId, clientId))
                    .map { buildWebEmailTemplate(it) }
        } ?: emptyList()
    }

    private fun getClientEmailTemplates(): List<WebEmailTemplate> {
        val clientId = AuthenticationUtils.clientID
        return clientId?.let { emailTemplateRepository.findByClientID(clientId).map { buildWebEmailTemplate(it) } }
                ?: emptyList()
    }

    fun saveEmailTemplate(webEmailTemplate: WebEmailTemplate): Long {
        val clientId = AuthenticationUtils.clientID
        if (clientId != null) {
            val nameExists = emailTemplateRepository.existsByNameAndClientID(webEmailTemplate.name, clientId)
            if (nameExists) {
                val error = ValidationError()
                error.addFieldError("name", "Template with name : ${webEmailTemplate.name} already exists")
                throw UndBusinessValidationException(error)
            }
        }
        val emailTemplate = buildEmailTemplate(webEmailTemplate)

        val persistedemailTemplate = emailTemplateRepository.save(emailTemplate)
        return persistedemailTemplate.id ?: -1
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
}