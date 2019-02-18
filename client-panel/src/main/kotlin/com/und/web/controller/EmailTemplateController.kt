package com.und.web.controller

import com.und.common.utils.loggerFor
import com.und.security.utils.AuthenticationUtils
import com.und.service.EmailTemplateService
import com.und.web.model.EmailTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

@CrossOrigin
@RestController
@RequestMapping("/client/email")
class EmailTemplateController {

    companion object {

        protected val logger = loggerFor(EmailTemplateController::class.java)
    }

    @Autowired
    private lateinit var emailTemplateService: EmailTemplateService

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/default-templates"])
    fun getDefaultEmailTemplates(): List<EmailTemplate> {
        logger.debug("Inside getDefaultEmailTemplates method")
        return emailTemplateService.getDefaultEmailTemplates()
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/templates"])
    fun getClientEmailTemplates(): List<EmailTemplate> {
        return emailTemplateService.getClientEmailTemplates()
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = ["/save-template"])
    fun saveEmailTemplate(@Valid @RequestBody emailTemplate: EmailTemplate): Long {
        var clientId=AuthenticationUtils.clientID
        if(clientId!=null) {
//            var exist = emailTemplateService.checkFromUserExistOrNot(clientId, emailTemplate.from)

            if (true) {
                return emailTemplateService.saveEmailTemplate(emailTemplate)
            }
        }
        return -1L
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/template/{id}"])
    fun getEmailTemplateById(@PathVariable id: Long): EmailTemplate {
        return emailTemplateService.getEmailTemplateById(id)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/user-event-attributes"])
    fun getUserEventAttributes() {
        emailTemplateService.getUserEventAttributes()
    }
}