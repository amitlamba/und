package com.und.web.controller

import com.und.common.utils.loggerFor
import com.und.service.EmailTemplateService
import com.und.web.model.EmailTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

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
    fun getClientEmailTemplates(@RequestParam(value = "id", required = false) id: Long? = null): List<EmailTemplate> {
        return emailTemplateService.getClientEmailTemplates(id)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = ["/save-template"])
    fun saveEmailTemplate(@RequestBody emailTemplate: EmailTemplate): Long {

        return emailTemplateService.saveEmailTemplate(emailTemplate)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/user-event-attributes"])
    fun getUserEventAttributes() {
        emailTemplateService.getUserEventAttributes()
    }
}