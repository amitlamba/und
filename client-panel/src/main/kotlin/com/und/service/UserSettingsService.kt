package com.und.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.model.Status
import com.und.model.jpa.ClientSettings
import com.und.model.jpa.ServiceProviderCredentials
import com.und.repository.jpa.ClientSettingsRepository
import com.und.web.model.ServiceProviderCredentials as WebServiceProviderCredentials
import com.und.repository.jpa.ServiceProviderCredentialsRepository
import com.und.web.model.AccountSettings
import com.und.web.model.EmailAddress
import com.und.web.model.UnSubscribeLink

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.annotation.PostConstruct
import com.fasterxml.jackson.module.kotlin.*
import com.und.common.utils.DateUtils
import com.und.common.utils.encrypt
import com.und.common.utils.loggerFor
import com.und.config.EventStream
import com.und.model.Email
import com.und.model.jpa.Client
import com.und.web.controller.exception.UndBusinessValidationException
import com.und.model.jpa.ClientSettingsEmail
import com.und.repository.jpa.ClientRepository
import com.und.repository.jpa.ClientSettingsEmailRepository
import com.und.security.utils.AuthenticationUtils
import com.und.web.controller.exception.WrongCredentialException
import com.und.web.model.ValidationError
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.support.MessageBuilder
import java.net.URLEncoder
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import javax.mail.AuthenticationFailedException
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.internet.InternetAddress

@Service
class UserSettingsService {

    @Autowired
    private lateinit var serviceProviderCredentialsRepository: ServiceProviderCredentialsRepository

    @Autowired
    private lateinit var clientSettingsRepository: ClientSettingsRepository

    @Autowired
    private lateinit var clientSettingsEmailRepository: ClientSettingsEmailRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var eventStream: EventStream

    @Autowired
    private lateinit var clientRepository: ClientRepository

    @Value("\${und.url.client}")
    lateinit var clientUrl: String

    private var emptyArrayJson: String = "[]"

    private var templateId=1L
    private var templateName="fromEmailVerification"
    private var expiration=24*60*60

    @PostConstruct
    fun setUp() {
        emptyArrayJson = objectMapper.writeValueAsString(emptyArray<String>())//"[]"//objectMapper.
    }

    fun getEmailServiceProvider(clientID: Long): List<WebServiceProviderCredentials> {
        val spCredsList = serviceProviderCredentialsRepository.findAllByClientIDAndServiceProviderType(clientID, ServiceProviderType.EMAIL_SERVICE_PROVIDER.desc)
        val wspCreds = mutableListOf<WebServiceProviderCredentials>()
        spCredsList.forEach { wspCreds.add(buildWebServiceProviderCredentials(it)) }
        return wspCreds
    }

    fun getEmailServiceProvider(clientID: Long, id: Long): WebServiceProviderCredentials? {
        val spCreds = serviceProviderCredentialsRepository.findAllByClientIDAndIdAndServiceProviderType(clientID, id, ServiceProviderType.EMAIL_SERVICE_PROVIDER.desc)
        return buildWebServiceProviderCredentials(spCreds!!)
    }

    fun saveEmailServiceProvider(webServiceProviderCredentials: WebServiceProviderCredentials, status:Status): Long? {
        webServiceProviderCredentials.status = status
        val serviceProviderCredentials = buildServiceProviderCredentials(webServiceProviderCredentials)
        val saved = serviceProviderCredentialsRepository.save(serviceProviderCredentials)
        return saved.id!!
    }

    fun getSmsServiceProvider(clientID: Long): List<WebServiceProviderCredentials> {
        val spCredsList = serviceProviderCredentialsRepository.findAllByClientIDAndServiceProviderType(clientID, ServiceProviderType.SMS_SERVICE_PROVIDER.desc)
        val wspCreds = mutableListOf<WebServiceProviderCredentials>()
        spCredsList.forEach { wspCreds.add(buildWebServiceProviderCredentials(it)) }
        return wspCreds
    }

    fun getSmsServiceProvider(clientID: Long, id: Long): WebServiceProviderCredentials? {
        val spCreds = serviceProviderCredentialsRepository.findAllByClientIDAndIdAndServiceProviderType(clientID, id, ServiceProviderType.SMS_SERVICE_PROVIDER.desc)
        return buildWebServiceProviderCredentials(spCreds!!)
    }

    fun saveSmsServiceProvider(webServiceProviderCredentials: WebServiceProviderCredentials): Long? {
        webServiceProviderCredentials.status = Status.ACTIVE
        val serviceProviderCredentials = buildServiceProviderCredentials(webServiceProviderCredentials)
        val saved = serviceProviderCredentialsRepository.save(serviceProviderCredentials)
        return saved.id!!
    }

    fun getNotificationServiceProvider(clientID: Long): List<WebServiceProviderCredentials> {
        val spCredsList = serviceProviderCredentialsRepository.findAllByClientIDAndServiceProviderType(clientID, ServiceProviderType.NOTIFICATION_SERVICE_PROVIDER.desc)
        val wspCreds = mutableListOf<WebServiceProviderCredentials>()
        spCredsList.forEach { wspCreds.add(buildWebServiceProviderCredentials(it)) }
        return wspCreds
    }

    fun getNotificationServiceProvider(clientID: Long, id: Long): WebServiceProviderCredentials? {
        val spCreds = serviceProviderCredentialsRepository.findAllByClientIDAndIdAndServiceProviderType(clientID, id, ServiceProviderType.NOTIFICATION_SERVICE_PROVIDER.desc)
        return buildWebServiceProviderCredentials(spCreds!!)
    }

    fun saveNotificationServiceProvider(webServiceProviderCredentials: WebServiceProviderCredentials): Long? {
        webServiceProviderCredentials.status = Status.ACTIVE
        val serviceProviderCredentials = buildServiceProviderCredentials(webServiceProviderCredentials)
        val saved = serviceProviderCredentialsRepository.save(serviceProviderCredentials)
        return saved.id!!
    }

    fun saveAndroidPushServiceProvider(webServiceProviderCredentials: WebServiceProviderCredentials): Long? {
        webServiceProviderCredentials.status = Status.ACTIVE
        val serviceProviderCredentials = buildServiceProviderCredentials(webServiceProviderCredentials)
        val saved = serviceProviderCredentialsRepository.save(serviceProviderCredentials)
        return saved.id!!
    }

    fun saveWebPushServiceProvider(webServiceProviderCredentials: WebServiceProviderCredentials): Long? {
        webServiceProviderCredentials.status = Status.ACTIVE
        val serviceProviderCredentials = buildServiceProviderCredentials(webServiceProviderCredentials)
        val saved = serviceProviderCredentialsRepository.save(serviceProviderCredentials)
        return saved.id!!
    }

    fun saveIOSPushServiceProvider(webServiceProviderCredentials: WebServiceProviderCredentials): Long? {
        webServiceProviderCredentials.status = Status.ACTIVE
        val serviceProviderCredentials = buildServiceProviderCredentials(webServiceProviderCredentials)
        val saved = serviceProviderCredentialsRepository.save(serviceProviderCredentials)
        return saved.id!!
    }

    fun getServiceProviders(clientID: Long): List<WebServiceProviderCredentials> {
        return serviceProviderCredentialsRepository.findAllByClientID(clientID).map { data -> buildWebServiceProviderCredentials(data) }
    }

    fun buildServiceProviderCredentials(webServiceProviderCredentials: WebServiceProviderCredentials): ServiceProviderCredentials {
        val spCreds = ServiceProviderCredentials()
        with(spCreds) {
            spCreds.appuserID = webServiceProviderCredentials.appuserID
            spCreds.clientID = webServiceProviderCredentials.clientID
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
        with(wspCreds) {
            wspCreds.appuserID = serviceProviderCredentials.appuserID
            wspCreds.clientID = serviceProviderCredentials.clientID

            wspCreds.id = serviceProviderCredentials.id
            wspCreds.serviceProvider = serviceProviderCredentials.serviceProvider
            wspCreds.serviceProviderType = serviceProviderCredentials.serviceProviderType
            wspCreds.status = serviceProviderCredentials.status
            wspCreds.credentialsMap = objectMapper.readValue(serviceProviderCredentials.credentialsMap)
        }
        return wspCreds
    }

    @Transactional
    fun saveAccountSettings(accountSettings: AccountSettings, clientID: Long?, userID: Long?) {
        //FIXME: Validate Timezone and Email Addresses
        if (clientID != null) {
            val clientSettings = ClientSettings()
            val clientSettingspersisted = clientSettingsRepository.findByClientID(clientID)
            clientSettings.id = clientSettingspersisted?.id
            clientSettings.clientID = clientID
            clientSettings.authorizedUrls = objectMapper.writeValueAsString(accountSettings.urls)
            clientSettings.timezone = accountSettings.timezone
            if (clientSettingspersisted == null) {
                clientSettingsRepository.save(clientSettings)
            } else {
                clientSettingsRepository.updateAccountSettings(clientSettings.authorizedUrls, clientSettings.timezone, clientID)
            }
        }
    }

    fun getAccountSettings(clientID: Long): Optional<AccountSettings> {
        val clientSettings = clientSettingsRepository.findByClientID(clientID)

        return if (clientSettings != null) {
            val setting =
                    AccountSettings(clientSettings.id, objectMapper.readValue(clientSettings.authorizedUrls
                            ?: emptyArrayJson), clientSettings.timezone)
            Optional.of(setting)
        } else Optional.empty()
    }

    @Transactional
    fun addSenderEmailAddress(emailAddress: EmailAddress, clientID: Long) {
        val emailExists = emailExists(emailAddress, clientID)
        if (emailExists) {
            val error = ValidationError()
            error.addFieldError("email", "Email : $emailAddress.personal already exist")
            throw UndBusinessValidationException(error)
        } else {
            val clientSettingEmail = ClientSettingsEmail()
            clientSettingEmail.email = emailAddress.address
            clientSettingEmail.address = emailAddress.personal
            clientSettingEmail.verified = emailAddress.status
            clientSettingEmail.clientId = clientID
            clientSettingsEmailRepository.save(clientSettingEmail)

        }

        //send verification link
        sendVerificationEmail(emailAddress, clientID)

    }

    @Transactional
    fun removeSenderEmailAddress(emailAddress: EmailAddress, clientID: Long) {

        val emailExists = emailExists(emailAddress, clientID)
        if (emailExists) {
            val email = clientSettingsEmailRepository.findByEmailAndClientIdAndDeleted(emailAddress.address, clientID, false)
            email?.deleted = true
            clientSettingsEmailRepository.save(email)

        } else {
            val error = ValidationError()
            error.addFieldError("email", "Email : $emailAddress.personal doesn't exist")
            throw UndBusinessValidationException(error)
        }

    }

    private fun emailExists(emailAddress: EmailAddress, clientID: Long) =
            clientSettingsEmailRepository.existsByEmailAndClientIdAndDeleted(emailAddress.address, clientID, false)


    fun saveUnSubscribeLink(request: UnSubscribeLink, clientID: Long?) {

        val clientSettings = clientSettingsRepository.findByClientID(clientID!!)
        if (clientSettings == null) {

            val clientSettingsNew = ClientSettings()
            clientSettingsNew.unSubscribeLink = request.unSubscribeLink
            clientSettingsNew.clientID = clientID
            clientSettingsRepository.save(clientSettingsNew)

        } else {

            clientSettings.unSubscribeLink = request.unSubscribeLink
            clientSettingsRepository.save(clientSettings)

        }

    }

    fun getUnSubscribeLink(clientID: Long?): Optional<UnSubscribeLink> {

        val clientSettings = clientSettingsRepository.findByClientID(clientID!!)
        val linkUrl = clientSettings?.unSubscribeLink
        val unSubscribeLink = linkUrl?.let { link ->
            val unSubscribeLink = UnSubscribeLink()
            unSubscribeLink.unSubscribeLink = link
            unSubscribeLink
        }
        return if (unSubscribeLink == null) Optional.empty() else Optional.of(unSubscribeLink)


    }

    fun getSenderEmailAddresses(clientID: Long, onlyVerified: Boolean): List<EmailAddress> {

        var emailAddresses=clientSettingsEmailRepository.findByClientIdAndDeleted(clientID, false)
        return emailAddresses?.filter { address->address.verified==onlyVerified }?.let {
            it.map { address ->EmailAddress(address.email ?: "", address.address ?: "",address.verified!!)
            }
        }?: emptyList()

    }


    fun getTimeZone(): ZoneId {
        val clientId = AuthenticationUtils.clientID
        val tz = clientId?.let {
            clientSettingsRepository.findByClientID(clientId)?.timezone
        } ?: TimeZone.getDefault().id
        return ZoneId.of(tz)

    }

    fun testConnection(serviceProviderCredential: com.und.web.model.ServiceProviderCredentials): Boolean {

        var port = Integer.parseInt(serviceProviderCredential.credentialsMap.get("port"))
        var host = serviceProviderCredential.credentialsMap.get("url")
        var username = serviceProviderCredential.credentialsMap.get("username")
        var password = serviceProviderCredential.credentialsMap.get("password")
        //var ssl = serviceProviderCredential.credentialsMap.get("ssl") as Boolean
        var protocaol = serviceProviderCredential.serviceProvider.toLowerCase()
        if (protocaol.equals("smtp")) {
            var props = Properties()
            props["mail.smtp.host"] = host
            props["mail.smtp.port"] = port
            props["mail.smtp.auth"] = "true"
            props["mail.smtp.starttls.enable"] = "true"
            props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory")
            props.put("mail.smtp.socketFactory.port",port)
//            if (ssl) {
//                props.put("mail.smtp.ssl.socketFactory.class","javax.net.ssl.SSLSocketFactory");
//                props.put("mail.smtp.ssl.socketFactory.port",port)
//                props["mail.smtp.ssl.enable"] = ssl
//            }


            try {
                val session = Session.getDefaultInstance(props)
                val transport = session.getTransport(protocaol)
                transport.connect(username, password)
                transport.close()
                return true
            } catch (e: AuthenticationFailedException) {
                throw WrongCredentialException("authentication failed")
                return false
            } catch (e: MessagingException) {
                throw WrongCredentialException(" Not valid credential")
                return false
            }

        }
        return true
    }

    fun sendVerificationEmail(emailAddress: EmailAddress, clientID: Long) {

        var data= mutableMapOf<String,Any>()
        var client = clientRepository.findById(clientID) as Client
        var toEmailAddress = InternetAddress(client.email)
        var fromEmailAddress = InternetAddress(emailAddress.address)
        var timeStamp = System.currentTimeMillis() / 1000
        var verificationCode = encrypt("$timeStamp||${emailAddress.address}||$clientID")
        var emailVerificationLink = "emailVerificationLink" to "${clientUrl}/setting/verifyemail/"+URLEncoder.encode(verificationCode,"UTF-8")
        var name = emailAddress.personal
        var emailSubject = "Verify from email Address"
        var emailBody="Hi ${name} \n Please verify your email by clicking on below link\n $emailVerificationLink"
//        data.put("name",name)
//        data.put("verificationLink",emailVerificationLink)
        var email = Email(clientID, fromEmailAddress, arrayOf(toEmailAddress), emailBody = emailBody,emailSubject = emailSubject,emailTemplateId = templateId,emailTemplateName = templateName)

        toVerificationKafka(email)
    }

    private fun toKafka(email: Email) {
        eventStream.clientEmailSend().send(MessageBuilder.withPayload(email).build())
    }

    fun updateStatusOfEmailSetting(timestamp:Long,mail:String,clientID: Long) {

        var emailSetting=clientSettingsEmailRepository.findByEmailAndClientId(mail,clientID)

        //check verification link is clik before 24 hour
        var currentTimeStamp=System.currentTimeMillis()/1000
        val expired = currentTimeStamp < timestamp + expiration
        if(!expired){
            //here we give an option in ui to resend verification link
            val validationError = ValidationError()
            validationError.addFieldError("emailVerification",
                    "Invalid Link, link has expired please request for new email")
            throw UndBusinessValidationException(validationError)
        }else{
            //update setting
            emailSetting.verified=true
            clientSettingsEmailRepository.save(emailSetting)
            //give a successfull message
        }
    }

    private fun toVerificationKafka(email:Email){
        eventStream.verificationEmailReceive().send(MessageBuilder.withPayload(email).build())
    }



}

enum class ServiceProviderType(val desc: String) {
    EMAIL_SERVICE_PROVIDER("Email Service Provider"),
    SMS_SERVICE_PROVIDER("SMS Service Provider"),
    NOTIFICATION_SERVICE_PROVIDER("Notification Service Provider"),
    ANDROID_PUSH_SERVICE_PROVIDER("Android Push Service Provider"),
    WEB_PUSH_SERVICE_PROVIDER("Web Push Service Provider"),
    IOS_PUSH_SERVICE_PROVIDER("iOS Push Service Provider")

}