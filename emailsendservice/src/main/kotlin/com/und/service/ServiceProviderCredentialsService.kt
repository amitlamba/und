package com.und.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.und.email.repository.jpa.ServiceProviderCredentialsRepository
import com.und.email.service.EmailHelperService
import com.und.model.jpa.*
import com.und.model.jpa.ServiceProviderCredentials
import com.und.model.utils.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import com.und.model.utils.ServiceProviderCredentials as WebServiceProviderCredentials

@Service
class ServiceProviderCredentialsService {


    @Autowired
    private lateinit var serviceProviderCredentialsRepository: ServiceProviderCredentialsRepository

    @Autowired
    private lateinit var emailHelperService: EmailHelperService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    fun getEmailProviderCredentials(clientId: Long): Any {
        val serviceProvider = findActiveEmailServiceProvider(clientId)
        return when (serviceProvider.serviceProvider) {
            ServiceProvider.AWS_SES_SMTP.desc -> objectMapper.readValue<EmailSESConfig>(serviceProvider.credentialsMap)
            ServiceProvider.SMTP.desc -> objectMapper.readValue<EmailSMTPConfig>(serviceProvider.credentialsMap)
            else -> throw Exception("No Email Provider for Client with ID $clientId")
        }
    }

    fun getSmsProviderCredentials(clientId: Long): Any {
        val serviceProvider = findActiveSMSServiceProvider(clientId)
        return when (serviceProvider.serviceProvider) {
            ServiceProvider.AWS_SNS.desc -> objectMapper.readValue<SmsSNSConfig>(serviceProvider.credentialsMap)
            else -> throw Exception("No Sms Provider for Client with ID $clientId")
        }
    }

    fun getNotificationProviderCredentials(clientId: Long): Any {
        val serviceProvider = findActiveNotificationServiceProvider(clientId)
        when (serviceProvider.serviceProvider) {
            ServiceProvider.GOOGLE_FCM.desc -> return objectMapper.readValue<GoogleFCMConfig>(serviceProvider.credentialsMap)
            else -> throw Exception("No Notification Provider for Client with ID $clientId")
        }
    }

    fun findActiveSMSServiceProvider(clientID: Long): ServiceProviderCredentials {
        val serviceCredOption = serviceProviderCredentialsRepository.findTop1ByClientIDAndServiceProviderTypeAndStatus(
                clientID, ServiceProviderType.SMS_SERVICE_PROVIDER.desc, Status.ACTIVE)
        return if (serviceCredOption.isPresent)
            return serviceCredOption.get()
        else ServiceProviderCredentials()
    }

    fun findActiveEmailServiceProvider(clientID: Long): ServiceProviderCredentials {

        val serviceCredOption = serviceProviderCredentialsRepository.findTop1ByClientIDAndServiceProviderTypeAndStatus(
                clientID, ServiceProviderType.EMAIL_SERVICE_PROVIDER.desc, Status.ACTIVE)
        return if (serviceCredOption.isPresent)
            return serviceCredOption.get()
        else ServiceProviderCredentials()
    }

    fun findActiveNotificationServiceProvider(clientID: Long): ServiceProviderCredentials {
        val serviceCredOption = serviceProviderCredentialsRepository.findTop1ByClientIDAndServiceProviderTypeAndStatus(
                clientID, ServiceProviderType.NOTIFICATION_SERVICE_PROVIDER.desc, Status.ACTIVE)
        return if (serviceCredOption.isPresent)
            return serviceCredOption.get()
        else ServiceProviderCredentials()
    }
    fun findActiveAndroidServiceProvider(clientID: Long,id: Long?): ServiceProviderCredentials {
//        val serviceCredOption = serviceProviderCredentialsRepository.findTop1ByClientIDAndServiceProviderTypeAndStatus(
//                clientID, ServiceProviderType.Android_Service_Provider.desc, Status.ACTIVE)
//
//        return if (serviceCredOption.isPresent)
//            return serviceCredOption.get()
//        else ServiceProviderCredentials()
        return this.getServiceProviderCredential(id,clientID,"Android Push Service Provider")
    }
    fun findActiveWebServiceProvider(clientID: Long,id: Long?): ServiceProviderCredentials {
        return this.getServiceProviderCredential(id,clientID,"Web Push Service Provider")
    }
    fun findActiveIosServiceProvider(clientID: Long,id: Long?): ServiceProviderCredentials {
        return this.getServiceProviderCredential(id,clientID,"iOS Push Service Provider")
    }


    enum class ServiceProviderType(val desc: String) {
        EMAIL_SERVICE_PROVIDER("Email Service Provider"),
        SMS_SERVICE_PROVIDER("SMS Service Provider"),
        NOTIFICATION_SERVICE_PROVIDER("Notification Service Provider"),
        Android_Service_Provider("Android Push Service Provider"),
        Webpush_Service_Provider("Web Push Service Provider"),
        Ios_Service_Provider("iOS Push Service Provider")

    }

    enum class ServiceProvider(val desc: String) {
        SMTP("SMTP"),
        AWS_SES_API("AWS - Simple Email Service (API)"),
        AWS_SES_SMTP("AWS - Simple Email Service (SMTP)"),
        AWS_SNS("AWS - Simple Notification Service"),
        GOOGLE_FCM("Google - FCM"),
        Twillio("Twillio"),
        Exotel("Exotel"),
        SendGrid("SendGrid")


    }

    /*fun saveSMTPEmailProviderCredentials(clientId: Long, appUserId: Long, emailSMTPConfig: EmailSMTPConfig): EmailSMTPConfig {
        var serviceProviderCredentials: ServiceProviderCredentials = ServiceProviderCredentials()
        serviceProviderCredentials.serviceProvider= "SMTP"
        serviceProviderCredentials.serviceProviderType= ServiceProviderType.EMAIL_SERVICE_PROVIDER
        serviceProviderCredentials.clientID=clientId
        serviceProviderCredentials.appuserID=appUserId
        serviceProviderCredentials.credentialsMap = objectMapper.writeValueAsString(emailSMTPConfig)
        serviceProviderCredentials.status= Status.ACTIVE
        emailSMTPConfig.serviceProviderCredentialsId = saveServiceProviderCredentials(serviceProviderCredentials)
        return emailSMTPConfig
    }

    fun saveSESEmailProviderCredentials(clientId: Long, appUserId: Long, emailSESConfig: EmailSESConfig): EmailSESConfig {
        var serviceProviderCredentials: ServiceProviderCredentials = ServiceProviderCredentials()
        serviceProviderCredentials.serviceProvider= ServiceProvider.AWS_SES
        serviceProviderCredentials.serviceProviderType= ServiceProviderType.EMAIL_SERVICE_PROVIDER
        serviceProviderCredentials.clientID=clientId
        serviceProviderCredentials.appuserID=appUserId
        serviceProviderCredentials.credentialsMap = objectMapper.writeValueAsString(emailSESConfig)
        serviceProviderCredentials.status= Status.ACTIVE
        emailSESConfig.serviceProviderCredentialsId = saveServiceProviderCredentials(serviceProviderCredentials)
        return emailSESConfig
    }

    fun saveSnsSmsProviderCredentials(clientId: Long, appUserId: Long, emailSESConfig: EmailSESConfig): EmailSESConfig {
        var serviceProviderCredentials: ServiceProviderCredentials = ServiceProviderCredentials()
        serviceProviderCredentials.serviceProvider= ServiceProvider.AWS_SNS
        serviceProviderCredentials.serviceProviderType= ServiceProviderType.SMS_SERVICE_PROVIDER
        serviceProviderCredentials.clientID=clientId
        serviceProviderCredentials.appuserID=appUserId
        serviceProviderCredentials.credentialsMap = objectMapper.writeValueAsString(emailSESConfig)
        serviceProviderCredentials.status= Status.ACTIVE
        emailSESConfig.serviceProviderCredentialsId = saveServiceProviderCredentials(serviceProviderCredentials)
        return emailSESConfig
    }*/

    /*private fun saveServiceProviderCredentials(serviceProviderCredentials: ServiceProviderCredentials): Long {
        val saved = serviceProviderCredentialsRepository.save(serviceProviderCredentials)
        return saved.id!!
    }*/

    fun buildServiceProviderCredentials(webServiceProviderCredentials: WebServiceProviderCredentials): ServiceProviderCredentials {
        val spCreds = ServiceProviderCredentials()
        with(spCreds) {
            spCreds.appuserID = webServiceProviderCredentials.appuserID
            spCreds.clientID = webServiceProviderCredentials.clientID
            spCreds.dateCreated = webServiceProviderCredentials.dateCreated
            spCreds.dateModified = webServiceProviderCredentials.dateModified
            spCreds.id = webServiceProviderCredentials.id
            spCreds.serviceProvider = webServiceProviderCredentials.serviceProvider
            spCreds.serviceProviderType = webServiceProviderCredentials.serviceProviderType
            spCreds.status = webServiceProviderCredentials.status
            spCreds.credentialsMap = objectMapper.writeValueAsString(webServiceProviderCredentials.credentialsMap)
        }
        return spCreds
    }

    fun buildWebServiceProviderCredentials(serviceProviderCredentials: ServiceProviderCredentials): WebServiceProviderCredentials {
        val wspCreds = WebServiceProviderCredentials()
        val clientId = serviceProviderCredentials.clientID
        if (clientId != null) {
            wspCreds.clientID = clientId
        }
        with(wspCreds) {
            wspCreds.appuserID = serviceProviderCredentials.appuserID
            wspCreds.dateCreated = serviceProviderCredentials.dateCreated
            wspCreds.dateModified = serviceProviderCredentials.dateModified
            wspCreds.id = serviceProviderCredentials.id
            wspCreds.serviceProvider = serviceProviderCredentials.serviceProvider
            wspCreds.serviceProviderType = serviceProviderCredentials.serviceProviderType
            wspCreds.status = serviceProviderCredentials.status
            wspCreds.credentialsMap = objectMapper.readValue(serviceProviderCredentials.credentialsMap)
        }
        return wspCreds
    }

    fun getServiceProviderCredentials(email: Email): com.und.model.utils.ServiceProviderCredentials {
//        val serviceProviderCred = this.findActiveEmailServiceProvider(email.clientID)
//        val serviceProviderCred=this.getServiceProviderCredential(email.serviceProviderId,email.clientID,"Email Service Provider")
        val serviceProviderCred=emailHelperService.getEmailServiceProviderCredentials(email.clientID,email.clientEmailSettingId!!)
        return this.buildWebServiceProviderCredentials(serviceProviderCred)
    }

    private fun getServiceProviderCredential(id:Long?,clientId: Long,type:String):ServiceProviderCredentials{
        var serviceProviderCred=ServiceProviderCredentials()
        if(id==null){
           var result= serviceProviderCredentialsRepository.findByIsdefaultTrue(type,clientId)
            if(result.isPresent) serviceProviderCred=result.get()
        }else{
            var result=serviceProviderCredentialsRepository.findById(id)
            if(result.isPresent) serviceProviderCred= result.get()
        }
        return serviceProviderCred
    }
    fun getServiceProviderCredentials(clientId: Long,id: Long?): com.und.model.utils.ServiceProviderCredentials {
//        val serviceProviderCred = this.findActiveSMSServiceProvider(clientId)
        val serviceProviderCred=this.getServiceProviderCredential(id, clientId,"SMS Service Provider")
        return this.buildWebServiceProviderCredentials(serviceProviderCred)
    }
}