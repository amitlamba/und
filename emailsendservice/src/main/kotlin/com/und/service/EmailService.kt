package com.und.service

import com.und.model.mongo.EmailStatus.NOT_SENT
import com.und.model.mongo.EmailStatus.SENT
import com.und.model.utils.Email
import com.und.model.utils.EmailSESConfig
import com.und.model.utils.EmailSMTPConfig
import com.und.model.utils.ServiceProviderCredentials
import com.und.repository.jpa.ClientSettingsRepository
import com.und.utils.loggerFor
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
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

    private var wspCredsMap: MutableMap<Long, ServiceProviderCredentials> = mutableMapOf()


    fun sendEmailBySMTP(emailSMTPConfig: EmailSMTPConfig, email: Email) {
        emailSendService.sendEmailBySMTP(emailSMTPConfig, email)
    }

    fun sendEmailByAWSSDK(emailSESConfig: EmailSESConfig, email: Email) {
        emailSendService.sendEmailByAWSSDK(emailSESConfig, email)
    }

    fun sendEmail(email: Email) {
        val mongoEmailId = emailHelperService.saveMailInMongo(email, NOT_SENT)
        //FIXME: cache the findByClientID clientSettings
        val clientSettings = clientSettingsRepository.findByClientID(email.clientID)
        if (StringUtils.isNotBlank(clientSettings?.unSubscribeLink))
            email.data["unsubscribeLink"] = emailHelperService.getUnsubscribeLink(clientSettings?.unSubscribeLink!!, email.clientID.toInt(), mongoEmailId!!)
        email.data["pixelTrackingPlaceholder"] = """<div><img src="""" + emailHelperService.getImageUrl(email.clientID.toInt(), mongoEmailId!!) + """">"""
        var emailToSend = emailHelperService.updateSubjectAndBody(email)
        emailToSend.emailBody = emailHelperService.trackAllURLs(emailToSend.emailBody!!, emailToSend.clientID, mongoEmailId)
        sendEmailWithoutTracking(emailToSend)
        emailHelperService.updateEmailStatus(mongoEmailId, SENT, email.clientID)
    }

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


}
