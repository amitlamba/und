package com.und.email.utility

import com.und.email.service.EmailSendService
import com.und.model.utils.*
import com.und.service.ServiceProviderCredentialsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class EmailServiceUtility {

    @Autowired
    private lateinit var emailSendService: EmailSendService

    @Autowired
    private lateinit var serviceProviderCredentialsService:ServiceProviderCredentialsService

    private var wspCredsMap: MutableMap<Long, ServiceProviderCredentials> = mutableMapOf()

    fun sendEmailBySMTP(emailSMTPConfig: EmailSMTPConfig, email: Email) {
        emailSendService.sendEmailBySMTP(emailSMTPConfig, email)
    }

    fun sendEmailByAWSSDK(emailSESConfig: EmailSESConfig, email: Email) {
        emailSendService.sendEmailByAWSSDK(emailSESConfig, email)
    }

    fun sendEmailBySendGrid(sendGridConfig: SendGridConfig,email: Email){
        emailSendService.sendEmailBySendGrid(sendGridConfig,email)
    }
    fun sendEmailWithoutTracking(email: Email) {
        val serviceProviderCredential = serviceProviderCredentials(email = email)
//        val serviceProviderCredential= emailHelperService.getEmailServiceProviderCredentials(email.clientID,email.clientEmailSettingId!!)
//        val spcrd=serviceProviderCredentialsService.buildWebServiceProviderCredentials(serviceProviderCredential)
        sendEmail(serviceProviderCredential, email)
    }

    private fun sendEmail(serviceProviderCredential: ServiceProviderCredentials, email: Email) {
        when (serviceProviderCredential.serviceProvider) {
            ServiceProviderCredentialsService.ServiceProvider.SMTP.desc,
            ServiceProviderCredentialsService.ServiceProvider.AWS_SES_SMTP.desc -> {
                val emailSMTPConfig = EmailSMTPConfig.build(serviceProviderCredential,email.clientEmailSettingId)
                sendEmailBySMTP(emailSMTPConfig, email)
            }
            ServiceProviderCredentialsService.ServiceProvider.AWS_SES_API.desc -> {
                val emailSESConfig = EmailSESConfig.build(serviceProviderCredential,email.clientEmailSettingId)
                sendEmailByAWSSDK(emailSESConfig, email)
            }
            ServiceProviderCredentialsService.ServiceProvider.SendGrid.desc ->{
                val sendGridConfig = SendGridConfig.build(serviceProviderCredential,email.clientEmailSettingId)
                sendEmailBySendGrid(sendGridConfig,email)
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