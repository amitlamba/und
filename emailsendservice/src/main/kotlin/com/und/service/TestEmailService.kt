package com.und.service

import com.und.common.utils.EmailServiceUtility
import com.und.model.mongo.EmailStatus
import com.und.model.utils.Email
import com.und.model.utils.eventapi.Event
import com.und.model.utils.eventapi.Identity
import com.und.repository.jpa.ClientSettingsRepository
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

    @Autowired
    private lateinit var clientSettingsRepository:ClientSettingsRepository

    @Autowired
    private lateinit var emailHelperService:EmailHelperService

    override fun sendEmail(email: Email) {

        val emailToSend = email.copy()
        val model = emailToSend.data
        emailToSend.eventUser?.let {
            model["user"] = it
        }
        //FIXME cache it
        val clientSettings = clientSettingsRepository.findByClientID(emailToSend.clientID)
        if (StringUtils.isNotBlank(clientSettings?.unSubscribeLink))
            model["unsubscribeLink"] = emailHelperService.getUnsubscribeLink(clientSettings?.unSubscribeLink!!, emailToSend.clientID, "")
        else model["unsubscribeLink"]=""
        emailToSend.emailSubject = templateContentCreationService.getTestEmailTemplateSubject(email.emailSubject?:"",model)
        emailToSend.emailBody = templateContentCreationService.getTestEmailTemplateBody(email.emailBody?:"",model)
        emailServiceUtility.sendEmailWithoutTracking(emailToSend)

    }
}