package com.und.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.model.Status
import com.und.model.jpa.ClientSettings
import com.und.model.jpa.ServiceProviderCredentials
import com.und.web.model.ServiceProviderCredentials as WebServiceProviderCredentials
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
import com.und.feign.FeignClientForAuthService
import com.und.model.Email
import com.und.model.jpa.Client
import com.und.web.controller.exception.UndBusinessValidationException
import com.und.model.jpa.ClientSettingsEmail
import com.und.model.jpa.security.User
import com.und.repository.jpa.*
import com.und.repository.redis.TokenIdentityRepository
import com.und.repository.redis.UserCacheRepository
import com.und.security.utils.AuthenticationUtils
import com.und.web.controller.exception.CustomException
import com.und.web.controller.exception.WrongCredentialException
import com.und.web.model.ValidationError
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.messaging.support.MessageBuilder
import org.springframework.security.access.AccessDeniedException
import java.net.URLEncoder
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import javax.mail.AuthenticationFailedException
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.Transport
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

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    lateinit var feignClientForAuthService: FeignClientForAuthService

    @Autowired
    lateinit var userCacheRepository: UserCacheRepository

    @Autowired
    lateinit var tokenIdentityRepository: TokenIdentityRepository

    @Value("\${und.url.client}")
    lateinit var clientUrl: String

    @Value("\${und.url.clientui}")
    lateinit var clientPanelUi:String

    private var emptyArrayJson: String = "[]"

    private var templateId = 6L
    private var templateName = "fromEmailVerification"
    private var expiration = 24 * 60 * 60

    companion object {
        var logger = LoggerFactory.getLogger(UserSettingsService::class.java)
    }

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

    @Transactional
    fun saveEmailServiceProvider(webServiceProviderCredentials: WebServiceProviderCredentials, status: Status) {
        webServiceProviderCredentials.status = status
        logger.info("Performing default check....")
        performIsDefaultCheckOnSpBeforeSave(webServiceProviderCredentials)
        logger.info("Performing default check successful. Building jpa sp ")
        val serviceProviderCredentials = buildServiceProviderCredentials(webServiceProviderCredentials)
        try {
            serviceProviderCredentialsRepository.save(serviceProviderCredentials)
            logger.info("Saved sp successfully.")
        } catch (ex: Throwable) {
            logger.error("Exception during saving service provider ${ex.message}")
            throw ex
        }
    }

    private fun performIsDefaultCheckOnSpBeforeSave(webServiceProviderCredentials: com.und.web.model.ServiceProviderCredentials) {
        logger.info("Is it default sp ${webServiceProviderCredentials.isDefault}")
        if (webServiceProviderCredentials.isDefault) {
            unMarkDefaultSp(webServiceProviderCredentials.serviceProviderType, webServiceProviderCredentials.isDefault, webServiceProviderCredentials.clientID!!)
        } else {
            logger.info("Getting list of sp's for client ${webServiceProviderCredentials.clientID}")
            var spList = serviceProviderCredentialsRepository.findAllByClientIDAndServiceProviderType(webServiceProviderCredentials.clientID!!, webServiceProviderCredentials.serviceProviderType)
            logger.info("Getting list of ${spList.size} sp's for client ${webServiceProviderCredentials.clientID} is successful.")
            if (spList.isEmpty()) webServiceProviderCredentials.isDefault = true
        }
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

    @Transactional
    fun saveSmsServiceProvider(webServiceProviderCredentials: WebServiceProviderCredentials): Long? {
        webServiceProviderCredentials.status = Status.ACTIVE
        performIsDefaultCheckOnSpBeforeSave(webServiceProviderCredentials)
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
        unMarkDefaultSp(webServiceProviderCredentials.serviceProviderType, webServiceProviderCredentials.isDefault, webServiceProviderCredentials.clientID!!)
        val saved = serviceProviderCredentialsRepository.save(serviceProviderCredentials)
        return saved.id!!
    }

    @Transactional
    fun saveAndroidPushServiceProvider(webServiceProviderCredentials: WebServiceProviderCredentials): Long? {
        webServiceProviderCredentials.status = Status.ACTIVE
        performIsDefaultCheckOnSpBeforeSave(webServiceProviderCredentials)
        val serviceProviderCredentials = buildServiceProviderCredentials(webServiceProviderCredentials)
        val saved = serviceProviderCredentialsRepository.save(serviceProviderCredentials)
        return saved.id!!
    }

    @Transactional
    fun saveWebPushServiceProvider(webServiceProviderCredentials: WebServiceProviderCredentials): Long? {
        webServiceProviderCredentials.status = Status.ACTIVE
        performIsDefaultCheckOnSpBeforeSave(webServiceProviderCredentials)
        val serviceProviderCredentials = buildServiceProviderCredentials(webServiceProviderCredentials)
        val saved = serviceProviderCredentialsRepository.save(serviceProviderCredentials)
        return saved.id!!
    }

    @Transactional
    fun saveIOSPushServiceProvider(webServiceProviderCredentials: WebServiceProviderCredentials): Long? {
        webServiceProviderCredentials.status = Status.ACTIVE
        performIsDefaultCheckOnSpBeforeSave(webServiceProviderCredentials)
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
            spCreds.name = webServiceProviderCredentials.name
//            spCreds.id = webServiceProviderCredentials.id
            spCreds.serviceProvider = webServiceProviderCredentials.serviceProvider
            spCreds.serviceProviderType = webServiceProviderCredentials.serviceProviderType
            spCreds.status = webServiceProviderCredentials.status
            spCreds.credentialsMap = objectMapper.writeValueAsString(webServiceProviderCredentials.credentialsMap)
            spCreds.isDefault = webServiceProviderCredentials.isDefault
        }
        return spCreds
    }

    fun buildWebServiceProviderCredentials(serviceProviderCredentials: ServiceProviderCredentials): WebServiceProviderCredentials {
        val wspCreds = WebServiceProviderCredentials()
        with(wspCreds) {
            wspCreds.appuserID = serviceProviderCredentials.appuserID
            wspCreds.clientID = serviceProviderCredentials.clientID
            wspCreds.name = serviceProviderCredentials.name
            wspCreds.id = serviceProviderCredentials.id
            wspCreds.serviceProvider = serviceProviderCredentials.serviceProvider
            wspCreds.serviceProviderType = serviceProviderCredentials.serviceProviderType
            wspCreds.status = serviceProviderCredentials.status
            wspCreds.credentialsMap = objectMapper.readValue(serviceProviderCredentials.credentialsMap)
            wspCreds.isDefault = serviceProviderCredentials.isDefault
        }
        return wspCreds
    }

//    @Transactional
    /*
    * TODO if user update his timezone then we are not replacing it.
    * */
    fun saveAccountSettings(accountSettings: AccountSettings, clientID: Long?, userID: Long?):Map<String,Any>? {
        //FIXME: Validate Timezone and Email Addresses
        if (clientID != null) {
            val clientSettings = ClientSettings()
            val clientSettingspersisted = clientSettingsRepository.findByClientID(clientID)
            clientSettings.id = clientSettingspersisted?.id
            clientSettings.clientID = clientID
            clientSettings.authorizedUrls = clientSettingspersisted?.authorizedUrls
            clientSettings.iosAppIds = clientSettingspersisted?.iosAppIds
            clientSettings.androidAppIds = clientSettingspersisted?.androidAppIds
            val token = userCacheRepository.findById(userID.toString()).get().loginKey
            val user = userRepository.findUser(clientID)
            var tokenList = mutableMapOf<String, Any>()
            accountSettings.urls?.let {
                if (it.isNotEmpty()) {
                    clientSettings.authorizedUrls = objectMapper.writeValueAsString(it)
//                    if (clientSettingspersisted?.authorizedUrls == null)
                    var id:Long?=null
                    try{
                        id=saveAccountSettings(clientSettings, accountSettings, clientSettingspersisted, clientID)
                        tokenList.set("web",feignClientForAuthService.refreshToken(false, "EVENT_WEB", token).body)
                        /*else*/ updateTokenIdentity(user, accountSettings.urls, "WEB")
                    }catch (ex:Exception){
                        if(id!=null){
                            clientSettingsRepository.deleteById((id?:-1L).toInt())
                        }else{
                            clientSettingsRepository.save(clientSettingspersisted)
                        }
                        throw ex
                    }

                }

            }
            accountSettings.andAppId?.let {
                if (it.isNotEmpty()) {
                    clientSettings.androidAppIds = objectMapper.writeValueAsString(it)
//                    if (clientSettingspersisted?.androidAppIds == null)
                    var id:Long?=null
                    try{
                        id=saveAccountSettings(clientSettings, accountSettings, clientSettingspersisted, clientID)
                        tokenList.set("android",feignClientForAuthService.refreshToken(false, "EVENT_ANDROID", token).body)
                        /*else*/ updateTokenIdentity(user, accountSettings.andAppId, "ANDROID")
                    }catch (ex:Exception){
                        if(id!=null){
                            clientSettingsRepository.deleteById((id?:-1L).toInt())
                        }else{
                            clientSettingsRepository.save(clientSettingspersisted)
                        }
                        throw ex
                    }
                }
            }
            accountSettings.iosAppId?.let {
                if (it.isNotEmpty()) {
                    clientSettings.iosAppIds = objectMapper.writeValueAsString(it)
//                if(clientSettingspersisted?.iosAppIds==null)
                    var id:Long?=null
                    try{
                        id=saveAccountSettings(clientSettings, accountSettings, clientSettingspersisted, clientID)
                        tokenList.set("ios",feignClientForAuthService.refreshToken(false,"EVENT_IOS",token).body)
                        /*else*/ updateTokenIdentity(user,accountSettings.iosAppId,"IOS")
                    }catch (ex:Exception){
                        if(id!=null){
                            clientSettingsRepository.deleteById((id?:-1L).toInt())
                        }else{
                            clientSettingsRepository.save(clientSettingspersisted)
                        }
                        throw ex
                    }

                }
            }

            return tokenList
        }
        return null
    }

    private fun saveAccountSettings(clientSettings: ClientSettings, accountSettings: AccountSettings, clientSettingspersisted: ClientSettings?, clientID: Long):Long? {
        clientSettings.timezone = accountSettings.timezone
        if (clientSettingspersisted == null) {
            val clientSetting=clientSettingsRepository.save(clientSettings)
            return clientSetting.id
        } else {
            clientSettingsRepository.updateAccountSettings(clientSettings.authorizedUrls, clientSettings.androidAppIds, clientSettings.iosAppIds, clientSettings.timezone, clientID)
            return null
        }
    }

    private fun updateTokenIdentity(user:Optional<User>,idenity:Array<String>,type:String) {
        user.ifPresent {
            var key: String? = null
            when (type) {
                "ANDROID" -> key = it.androidKey
                "IOS" -> key = it.iosKey
                "WEB" -> key = it.key
            }
            if (key != null) {
                var user = tokenIdentityRepository.findById(key)
                user.ifPresent {
                    it.identity = idenity
                    tokenIdentityRepository.save(it)
                }
            }
        }
    }

    fun getAccountSettings(clientID: Long): Optional<AccountSettings> {
        val clientSettings = clientSettingsRepository.findByClientID(clientID)

        return if (clientSettings != null) {
            val setting =
                    AccountSettings(clientSettings.id,
                            objectMapper.readValue(clientSettings.authorizedUrls ?: emptyArrayJson),
                            clientSettings.timezone,
                            objectMapper.readValue(clientSettings.androidAppIds ?: emptyArrayJson),
                            objectMapper.readValue(clientSettings.iosAppIds ?: emptyArrayJson))
            Optional.of(setting)
        } else Optional.empty()
    }

    @Transactional
    fun addSenderEmailAddress(emailAddress: EmailAddress, clientID: Long) {
        val emailExists = emailExists(emailAddress, clientID)
        if (emailExists) {
//            val error = ValidationError()
//            error.addFieldError("email", "Email : $emailAddress.personal already exist")
//            throw UndBusinessValidationException(error)
            throw CustomException("Email : ${emailAddress.address} already exist")
        } else {
            val serviceProviderId = emailAddress.serviceProviderId
            val serviceProvider = serviceProviderCredentialsRepository.findByIdAndClientID(serviceProviderId, clientID)
            if (serviceProvider != null) {

                val clientSettingEmail = ClientSettingsEmail()
                clientSettingEmail.email = emailAddress.address
                clientSettingEmail.address = emailAddress.personal
                clientSettingEmail.verified = emailAddress.status
                clientSettingEmail.clientId = clientID
                clientSettingEmail.serviceProviderId = emailAddress.serviceProviderId
                try {
                    val persistedSetting = clientSettingsEmailRepository.save(clientSettingEmail)
                    sendVerificationEmail(emailAddress, clientID, persistedSetting.id!!)
                }catch (ex:Exception){
                    throw CustomException("Error during persisting client setting ${ex.message}")
                }
            } else {
                throw CustomException("Please choose correct settings")
            }


        }


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

        var emailAddresses = clientSettingsEmailRepository.findByClientIdAndDeleted(clientID, false)
        return emailAddresses?.filter { address -> address.verified == onlyVerified }?.let {
            it.map { address ->
                EmailAddress(address.email ?: "", address.address ?: "", address.serviceProviderId!!, address.verified!!)
            }
        } ?: emptyList()

    }


    fun getTimeZone(): ZoneId {
        val clientId = AuthenticationUtils.clientID
        val tz = clientId?.let {
            clientSettingsRepository.findByClientID(clientId)?.timezone
        } ?: TimeZone.getDefault().id
        return ZoneId.of(tz)

    }

    fun getTimeZoneByClientId(clientID: Long): ZoneId {
        val tz = clientID.let {
            clientSettingsRepository.findByClientID(clientID)?.timezone
        } ?: TimeZone.getDefault().id
        return ZoneId.of(tz)

    }
    @Throws(WrongCredentialException::class)
    fun testConnection(serviceProviderCredential: com.und.web.model.ServiceProviderCredentials): Boolean {

        var port = Integer.parseInt(serviceProviderCredential.credentialsMap.get("port"))
        var host = serviceProviderCredential.credentialsMap.get("url")
        var username = serviceProviderCredential.credentialsMap.get("username")
        var password = serviceProviderCredential.credentialsMap.get("password")
        var ssl = serviceProviderCredential.credentialsMap.get("security") ?: "NONE"
        var protocaol = serviceProviderCredential.serviceProvider.toLowerCase()
        if (protocaol.equals("smtp")) {
            var props = Properties()
            props["mail.smtp.auth"] = true
            props["mail.smtp.host"] = host
            props["mail.smtp.port"] = port
            props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"

            when (Security.valueOf(ssl)) {
                Security.SSL, Security.TLS -> {
                    props["mail.smtp.ssl.enable"] = true
                    props["mail.smtp.starttls.enable"] = false
                }
                Security.STARTTLS -> {
                    props["mail.smtp.ssl.enable"] = false
                    props["mail.smtp.starttls.enable"] = true
                }
                Security.NONE -> {
                    props["mail.smtp.ssl.enable"] = false
                    props["mail.smtp.starttls.enable"] = false
                }
            }
            var transport:Transport?=null
            try {
                val session = Session.getInstance(props)
                // for debugging purpose session.debug=true
                transport = session.getTransport(protocaol)
                transport.connect(username, password)
                logger.info("Test connection for sp $username is successful")
                return true
            } catch (e: AuthenticationFailedException) {
                throw WrongCredentialException("authentication failed")
            } catch (e: MessagingException) {
                throw WrongCredentialException(" Not valid credential")
            }finally {
                transport?.close()
            }

        }else{
            return true
        }
    }

    fun sendVerificationEmail(emailAddress: EmailAddress, clientID: Long,id:Long) {
        var data = mutableMapOf<String, Any>()
        var client = clientRepository.findById(clientID)
        if (client.isPresent) {
            var client = client.get()
            var toEmailAddress = InternetAddress(client.email)
            var fromEmailAddress = InternetAddress(emailAddress.address)
            var timeStamp = System.currentTimeMillis() / 1000
            var verificationCode = encrypt("$timeStamp||${emailAddress.address}||$clientID")
            var emailVerificationLink = "${clientPanelUi}verifyemail?email=addfemail&code="+URLEncoder.encode(verificationCode, "UTF-8")
            var name = emailAddress.personal
//            var emailSubject = "Verify from email Address"
//            var emailBody="Hi ${name} \n Please verify your email by clicking on below link\n $emailVerificationLink"
            data.put("name", name)
            data.put("address", fromEmailAddress.address)
            data.put("emailVerificationLink", emailVerificationLink)
            var email = Email(
                    clientID = clientID,
                    fromEmailAddress = fromEmailAddress,
                    toEmailAddresses = arrayOf(toEmailAddress),
                    emailTemplateId = templateId,
                    emailTemplateName = templateName,
                    data = data,
                    clientEmailSettingId = id)
            logger.info("Sending from email address varification to ${client.email}")
            toKafka(email)
        }


    }

    private fun toKafka(email: Email) {
        eventStream.clientEmailSend().send(MessageBuilder.withPayload(email).build())
    }

    fun updateStatusOfEmailSetting(timestamp: Long, mail: String, clientID: Long) {

        var emailSetting = clientSettingsEmailRepository.findByEmailAndClientId(mail, clientID)

        //check verification link is clik before 24 hour
        var currentTimeStamp = System.currentTimeMillis() / 1000
        val expired = currentTimeStamp < timestamp + expiration
        if (!expired) {
            //here we give an option in ui to resend verification link
            val validationError = ValidationError()
            validationError.addFieldError("emailVerification",
                    "Invalid Link, link has expired please request for new email")
            throw UndBusinessValidationException(validationError)
        } else {
            //update setting
            emailSetting.verified = true
            clientSettingsEmailRepository.save(emailSetting)
            //give a successfull message
        }
    }

    @Transactional
    fun markDefault(type: String, id: Long, default: Boolean) {
        var clientID = AuthenticationUtils.clientID ?: throw AccessDeniedException("")
        try {
            if (default) {
                //check sp with this id present or not
                var result = serviceProviderCredentialsRepository.findById(id)
                if (result.isPresent) {
                    serviceProviderCredentialsRepository.unMarkDefaultSp(type, clientID)
                    serviceProviderCredentialsRepository.markSPDefault(id)
                } else {
                    throw CustomException("Service provider with ${id} not exists.")
                }
            } else {
                //This step is ui dependent how the default action is implemented.
                // if we give only check box then no need but if we give drop down with true false it needed.
                var result = serviceProviderCredentialsRepository.findById(id)
                result.ifPresent {
                    if (it.isDefault) {
                        throw CustomException("Choose a default service provider.")
                    }
                }
            }

        } catch (ex: Exception) {
            throw CustomException("Error occur during persisting your change. Try again. ${ex.message}")
        }
    }

    //call this method from save service provider in a transaction.
    fun unMarkDefaultSp(type: String, isDefault: Boolean, clientID: Long) {
        if (isDefault) {
            //unmark other default service provider of this type for this client
            logger.info("Unmarking... sp for client $clientID")
            serviceProviderCredentialsRepository.unMarkDefaultSp(type, clientID)
            logger.info("Unmarking sp for client $clientID is successful.")
        }
    }

    fun getIosServiceProviders(clientId: Long): List<WebServiceProviderCredentials> {
        val spCredsList = serviceProviderCredentialsRepository.findAllByClientIDAndServiceProviderType(clientId, ServiceProviderType.IOS_PUSH_SERVICE_PROVIDER.desc)
        val wspCreds = mutableListOf<WebServiceProviderCredentials>()
        spCredsList.forEach { wspCreds.add(buildWebServiceProviderCredentials(it)) }
        return wspCreds
    }

    fun getWebServiceProviders(clientId: Long): List<WebServiceProviderCredentials> {
        val spCredsList = serviceProviderCredentialsRepository.findAllByClientIDAndServiceProviderType(clientId, ServiceProviderType.WEB_PUSH_SERVICE_PROVIDER.desc)
        val wspCreds = mutableListOf<WebServiceProviderCredentials>()
        spCredsList.forEach { wspCreds.add(buildWebServiceProviderCredentials(it)) }
        return wspCreds
    }

    fun getAndroidServiceProviders(clientId: Long): List<WebServiceProviderCredentials> {
        val spCredsList = serviceProviderCredentialsRepository.findAllByClientIDAndServiceProviderType(clientId, ServiceProviderType.ANDROID_PUSH_SERVICE_PROVIDER.desc)
        val wspCreds = mutableListOf<WebServiceProviderCredentials>()
        spCredsList.forEach { wspCreds.add(buildWebServiceProviderCredentials(it)) }
        return wspCreds
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

enum class KEYTYPE {
    ADMIN_LOGIN,
    EVENT_ANDROID,
    EVENT_IOS,
    EVENT_WEB
}


enum class Security {
    SSL, TLS, STARTTLS, NONE
}