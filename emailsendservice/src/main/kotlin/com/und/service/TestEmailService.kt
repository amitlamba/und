package com.und.service

import com.und.common.utils.EmailServiceUtility
import com.und.model.mongo.EmailStatus
import com.und.model.utils.Email
import com.und.model.utils.eventapi.Event
import com.und.model.utils.eventapi.Identity
import org.apache.commons.lang.StringUtils
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils

@Service("testemailservice")
class TestEmailService:CommonEmailService {

    @Autowired
    private lateinit var emailServiceUtility: EmailServiceUtility

    @Autowired
    private lateinit var templateContentCreationService: TemplateContentCreationService

    override fun sendEmail(email: Email) {

        val emailToSend = email.copy()
        val model = emailToSend.data
        emailToSend.eventUser?.let {
            model["user"] = it
        }
        emailToSend.emailSubject = templateContentCreationService.getTestEmailTemplateSubject(email.emailSubject?:"",model)
        emailToSend.emailBody = templateContentCreationService.getTestEmailTemplateSubject(email.emailBody?:"",model)
        emailServiceUtility.sendEmailWithoutTracking(emailToSend)

    }
}