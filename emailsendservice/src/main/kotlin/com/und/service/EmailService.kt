package com.und.service

import com.und.model.mongo.EmailStatus.NOT_SENT
import com.und.model.mongo.EmailStatus.SENT
import com.und.model.utils.Email
import com.und.model.utils.EmailSESConfig
import com.und.model.utils.EmailSMTPConfig
import com.und.model.utils.ServiceProviderCredentials
import com.und.repository.jpa.ClientSettingsRepository
import com.und.repository.jpa.EmailTemplateRepository
import com.und.utils.loggerFor
import org.apache.commons.lang.StringUtils
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.mail.internet.InternetAddress
import com.amazonaws.services.simpleemail.model.Message as SESMessage


@Service
class EmailService {
    companion object {
        protected val logger = loggerFor(EmailService::class.java)
    }


    @Autowired
    private lateinit var serviceProviderCredentialsService: ServiceProviderCredentialsService

    @Autowired
    private lateinit var clientSettingsRepository: ClientSettingsRepository

    @Autowired
    private lateinit var emailHelperService: EmailHelperService

    @Autowired
    private lateinit var emailSendService: EmailSendService

    @Autowired
    private lateinit var emailTemplateRepository: EmailTemplateRepository

    private var wspCredsMap: MutableMap<Long, ServiceProviderCredentials> = mutableMapOf()


    fun sendEmailBySMTP(emailSMTPConfig: EmailSMTPConfig, email: Email) {
        emailSendService.sendEmailBySMTP(emailSMTPConfig, email)
    }

    fun sendEmailByAWSSDK(emailSESConfig: EmailSESConfig, email: Email) {
        emailSendService.sendEmailByAWSSDK(emailSESConfig, email)
    }



    fun sendEmail(email: Email) {


         fun String.addUrlTracking(uniqueTrackingId:String ):String {
            return emailHelperService.trackAllURLs(this, email.clientID, uniqueTrackingId)
        }
         fun String.addPixelTracking(uniqueTrackingId:String ):String {
            return emailHelperService.addPixelTracking(this, email.clientID, uniqueTrackingId)
        }

        val emailToSend = email.copy()
        val model = emailToSend.data
        //FIXME: cache the findByClientID clientSettings
        val clientSettings = clientSettingsRepository.findByClientID(emailToSend.clientID)
        val mongoEmailId = ObjectId().toString()
        emailToSend.eventUser?.let {
            model["user"] = it
        }
        if (StringUtils.isNotBlank(clientSettings?.unSubscribeLink))
            model["unsubscribeLink"] = emailHelperService.getUnsubscribeLink(clientSettings?.unSubscribeLink!!, emailToSend.clientID, mongoEmailId)

        model["pixelTrackingPlaceholder"] = """<div><img src="""" + emailHelperService.getImageUrl(emailToSend.clientID, mongoEmailId) + """">"""

        val (subject, body) = emailHelperService.subjectAndBody(emailToSend)


        emailToSend.emailBody = body.addUrlTracking(mongoEmailId).addPixelTracking(mongoEmailId)
        emailToSend.emailSubject = subject

        if(isSytemClient(email)) {
            val template = emailTemplateRepository.findByIdAndClientID(email.emailTemplateId, email.clientID)
            val from = template.map { it.from }
            if(from.isPresent) {
                emailToSend.fromEmailAddress = InternetAddress(from.get())
            } else throw Exception("from email for template id ${email.emailTemplateId} is not present for system user")
        }

        emailHelperService.saveMailInMongo(emailToSend, NOT_SENT, mongoEmailId)
        sendEmailWithoutTracking(emailToSend)
        emailHelperService.updateEmailStatus(mongoEmailId, SENT, emailToSend.clientID)
    }

    private fun isSytemClient(email: Email) = email.clientID == 1L

    fun sendEmailWithoutTracking(email: Email) {
        val serviceProviderCredential = serviceProviderCredentials(email = email)
        sendEmail(serviceProviderCredential, email)
    }

    private fun sendEmail(serviceProviderCredential: ServiceProviderCredentials, email: Email) {
        when (serviceProviderCredential.serviceProvider) {
            ServiceProviderCredentialsService.ServiceProvider.SMTP.desc,
            ServiceProviderCredentialsService.ServiceProvider.AWS_SES_SMTP.desc -> {
                val emailSMTPConfig = EmailSMTPConfig.build(serviceProviderCredential)
                sendEmailBySMTP(emailSMTPConfig, email)
            }
            ServiceProviderCredentialsService.ServiceProvider.AWS_SES_API.desc -> {
                val emailSESConfig = EmailSESConfig.build(serviceProviderCredential)
                sendEmailByAWSSDK(emailSESConfig, email)
            }

        }
    }

    private fun serviceProviderCredentials(email: Email): ServiceProviderCredentials {
        synchronized(email.clientID) {
            //TODO: This code can be cached in Redis
            if (!wspCredsMap.containsKey(email.clientID)) {
                val webServiceProviderCred = serviceProviderCredentialsService.getServiceProviderCredentials(email)
                wspCredsMap[email.clientID] = webServiceProviderCred
            }
        }
        return wspCredsMap[email.clientID]!!
    }

    fun sendVerificationEmail(email: Email){
        //update subject and body using template
        var templateId=email.emailTemplateId
        var templateName=email.emailTemplateName

        //var emailToSend=emailHelperService.updateSubjectAndBody(email)
        sendEmailWithoutTracking(email)
    }
}
