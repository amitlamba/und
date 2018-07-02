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
import com.und.web.controller.exception.UndBusinessValidationException
import com.und.model.jpa.ClientSettingsEmail
import com.und.repository.jpa.ClientSettingsEmailRepository
import com.und.web.model.ValidationError

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

    private var emptyArrayJson: String = "[]"


    private val emailServiceProvider = "Email Service Provider"
    private val smsServiceProvider = "Sms Service Provider"

    @PostConstruct
    fun setUp() {
        emptyArrayJson = objectMapper.writeValueAsString(emptyArray<String>())//"[]"//objectMapper.
    }

    fun getEmailServiceProvider(clientID: Long): List<WebServiceProviderCredentials> {
        val spCredsList = serviceProviderCredentialsRepository.findAllByClientIDAndServiceProviderType(clientID, emailServiceProvider)
        val wspCreds = mutableListOf<WebServiceProviderCredentials>()
        spCredsList.forEach { wspCreds.add(buildWebServiceProviderCredentials(it)) }
        return wspCreds
    }

    fun getEmailServiceProvider(clientID: Long, id: Long): WebServiceProviderCredentials? {
        val spCreds = serviceProviderCredentialsRepository.findAllByClientIDAndIdAndServiceProviderType(clientID, id, emailServiceProvider)
        return buildWebServiceProviderCredentials(spCreds!!)
    }

    fun saveEmailServiceProvider(webServiceProviderCredentials: WebServiceProviderCredentials): Long? {
        webServiceProviderCredentials.status = Status.ACTIVE
        val serviceProviderCredentials = buildServiceProviderCredentials(webServiceProviderCredentials)
        val saved = serviceProviderCredentialsRepository.save(serviceProviderCredentials)
        return saved.id!!
    }

    fun getSmsServiceProvider(clientID: Long): List<WebServiceProviderCredentials> {
        val spCredsList = serviceProviderCredentialsRepository.findAllByClientIDAndServiceProviderType(clientID, smsServiceProvider)
        val wspCreds = mutableListOf<WebServiceProviderCredentials>()
        spCredsList.forEach { wspCreds.add(buildWebServiceProviderCredentials(it)) }
        return wspCreds
    }

    fun getSmsServiceProvider(clientID: Long, id: Long): WebServiceProviderCredentials? {
        val spCreds = serviceProviderCredentialsRepository.findAllByClientIDAndIdAndServiceProviderType(clientID, id, smsServiceProvider)
        return buildWebServiceProviderCredentials(spCreds!!)
    }

    fun saveSmsServiceProvider(webServiceProviderCredentials: WebServiceProviderCredentials): Long? {
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
        if(clientID != null) {
            val clientSettings = ClientSettings()
            val clientSettingspersisted = clientSettingsRepository.findByClientID(clientID)
            clientSettings.id = clientSettingspersisted?.id
            clientSettings.clientID = clientID
            clientSettings.authorizedUrls = objectMapper.writeValueAsString(accountSettings.urls)
            clientSettings.timezone = accountSettings.timezone
            if(clientSettingspersisted == null) {
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
            clientSettingEmail.verified = false
            clientSettingEmail.clientId = clientID
            clientSettingsEmailRepository.save(clientSettingEmail)

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
        val unSubscribeLink  = linkUrl?.let { link ->
            val unSubscribeLink = UnSubscribeLink()
            unSubscribeLink.unSubscribeLink = link
            unSubscribeLink
        }
        return if(unSubscribeLink == null) Optional.empty() else Optional.of(unSubscribeLink)


    }

     fun getSenderEmailAddresses(clientID: Long): List<EmailAddress> {
        val emailAddresses = clientSettingsEmailRepository.findByClientIdAndDeleted(clientID, false)
        return emailAddresses?.let {
            it.map { address -> EmailAddress(address.email ?: "", address.address ?: "") }
        } ?: emptyList()

    }

}