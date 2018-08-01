package com.und.web.controller

import com.und.common.utils.decrypt
import com.und.security.utils.AuthenticationUtils
import com.und.service.UserSettingsService
import com.und.web.model.AccountSettings
import com.und.web.model.EmailAddress
import com.und.web.model.ServiceProviderCredentials
import com.und.web.model.UnSubscribeLink
import org.apache.kafka.common.errors.InvalidRequestException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.net.URLDecoder
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid

@CrossOrigin
@RestController
@RequestMapping("/setting")
class UserSettingsController {

    @Autowired
    private lateinit var userSettingsService: UserSettingsService


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/service-providers"])
    fun getServiceProviders(): List<ServiceProviderCredentials> {
        val clientID = AuthenticationUtils.clientID
        return userSettingsService.getServiceProviders(clientID!!)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/email-service-providers"])
    fun getEmailServiceProviders(): List<ServiceProviderCredentials> {
        val clientID = AuthenticationUtils.clientID
        return userSettingsService.getEmailServiceProvider(clientID!!)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/email-service-provider/{id}"])
    fun getEmailServiceProvider(@PathVariable id: Long): ServiceProviderCredentials? {
        val clientID = AuthenticationUtils.clientID
        return userSettingsService.getEmailServiceProvider(clientID!!, id)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = ["/email-service-provider/save"])
    fun saveEmailServiceProvider(@Valid @RequestBody serviceProviderCredentials: ServiceProviderCredentials): Long? {
        val clientID = AuthenticationUtils.clientID
        val userID = AuthenticationUtils.principal.id
        serviceProviderCredentials.appuserID = userID
        serviceProviderCredentials.clientID = clientID
        serviceProviderCredentials.serviceProviderType = "Email Service Provider"
        //check credential are correct or not
        var success=userSettingsService.testConnection(serviceProviderCredentials)
        if(success)
        return userSettingsService.saveEmailServiceProvider(serviceProviderCredentials)
        return 0L
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/sms-service-providers"])
    fun getSmsServiceProviders(): List<ServiceProviderCredentials> {
        val clientID = AuthenticationUtils.clientID
        return userSettingsService.getSmsServiceProvider(clientID!!)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/sms-service-provider/{id}"])
    fun getSmsServiceProvider(@PathVariable id: Long): ServiceProviderCredentials? {
        val clientID = AuthenticationUtils.clientID
        return userSettingsService.getSmsServiceProvider(clientID!!, id)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = ["/sms-service-provider/save"])
    fun saveSmsServiceProvider(@Valid @RequestBody serviceProviderCredentials: ServiceProviderCredentials): Long? {
        val clientID = AuthenticationUtils.clientID
        val userID = AuthenticationUtils.principal.id
        serviceProviderCredentials.appuserID = userID
        serviceProviderCredentials.clientID = clientID
        serviceProviderCredentials.serviceProviderType = "Sms Service Provider"
        return userSettingsService.saveSmsServiceProvider(serviceProviderCredentials)
    }



    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = ["/senders-email/add"])
    fun addSendersEmail(@RequestBody email: EmailAddress) {
        val clientID = AuthenticationUtils.clientID
        userSettingsService.addSenderEmailAddress(email, clientID!!)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/senders-email/list"])
    fun getSendersEmailList(): List<EmailAddress> {
        val clientID = AuthenticationUtils.clientID
        return userSettingsService.getSenderEmailAddresses(clientID!!)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = ["/senders-email/delete"])
    fun deleteSendersEmail(@RequestBody email: EmailAddress) {
        val clientID = AuthenticationUtils.clientID
        userSettingsService.removeSenderEmailAddress(email, clientID!!)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = ["/account-settings/save"])
    fun saveAccountSettings(@RequestBody accountSettings: AccountSettings) {
        val clientID = AuthenticationUtils.clientID
        val userID = AuthenticationUtils.principal.id
        return userSettingsService.saveAccountSettings(accountSettings, clientID, userID)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/account-settings/get"])
    fun getAccountSettings(): AccountSettings? {
        val clientID = AuthenticationUtils.clientID
        val userID = AuthenticationUtils.principal.id
        if (clientID != null) {
            val optionalSettings = userSettingsService.getAccountSettings(clientID)
            return if (optionalSettings.isPresent) optionalSettings.get() else null

        }
        return null
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = ["/unsubscribe-link/save"])
    fun saveUnSubscribeLink(@Valid @RequestBody request: UnSubscribeLink) {
        val clientID = AuthenticationUtils.clientID
        userSettingsService.saveUnSubscribeLink(request, clientID)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/unsubscribe-link/get"])
    fun getUnsubscribeLink(): UnSubscribeLink? {
        val clientID = AuthenticationUtils.clientID
        val linkOptional = userSettingsService.getUnSubscribeLink(clientID)
        return if(linkOptional.isPresent) linkOptional.get() else null
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/verifyemail/{link}"])
    fun verifyEmail(@PathVariable(value = "link")link:String){

        var decodeString= URLDecoder.decode(link,"UTF-8")
        var decryptString= decrypt(decodeString)
        var details=decryptString.split("||")
        var timeStamp=details[0].toLong()
        var mail=details[1]
        var clientId=details[2].toLong()

        userSettingsService.updateStatusOfEmailSetting(timeStamp,mail,clientId)


    }

}

