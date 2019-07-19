package com.und.service

import com.und.email.utility.EmailServiceUtility
import com.und.common.utils.ReplaceNullPropertyOfEventUser
import com.und.email.service.EmailHelperService
import com.und.email.service.EmailService
import com.und.model.utils.Email
import com.und.repository.jpa.ClientSettingsRepository
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.regex.Pattern

@Service("testemailservice")
class TestEmailService:CommonEmailService {

    @Autowired
    private lateinit var emailServiceUtility: EmailServiceUtility

    @Autowired
    private lateinit var templateContentCreationService: TemplateContentCreationService

    @Autowired
    private lateinit var clientSettingsRepository:ClientSettingsRepository

    @Autowired
    private lateinit var emailHelperService: EmailHelperService

    @Autowired
    private lateinit var emailService: EmailService

    override fun sendEmail(email: Email) {

        val emailToSend = email.copy()
        val model = emailToSend.data
        val variable= getVariableFromTemplate(email.emailSubject?:"",email.emailBody?:"")
        val user=ReplaceNullPropertyOfEventUser.replaceNullPropertyOfEventUser(email.eventUser,variable)
        user?.let {
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

    fun getVariableFromTemplate(subject:String,body:String):Set<String>{
        val listOfVariable = mutableSetOf<String>()
            val regex="(\\$\\{.*?\\})"
            val pattern = Pattern.compile(regex)

            val subjectMatcher = pattern.matcher(subject)
            val bodyMatcher = pattern.matcher(body)
            var i=0
            while (subjectMatcher.find()){
                listOfVariable.add(subjectMatcher.group(i+1))
            }
            i=0
            while (bodyMatcher.find()){
                listOfVariable.add(bodyMatcher.group(i+1))
            }
        return listOfVariable
    }

}