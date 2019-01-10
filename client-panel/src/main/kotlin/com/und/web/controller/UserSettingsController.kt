package com.und.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.common.utils.decrypt
import com.und.model.Status
import com.und.security.utils.AuthenticationUtils
import com.und.service.CampaignService
import com.und.service.ServiceProviderType
import com.und.service.UserSettingsService
import com.und.web.controller.exception.CustomException
import com.und.web.model.*
import org.apache.kafka.common.errors.InvalidRequestException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.AuthenticationException
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

    @Autowired
    private lateinit var campaignService: CampaignService

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
        val clientID = AuthenticationUtils.clientID ?: throw AccessDeniedException("")
        val userID = AuthenticationUtils.principal.id
        serviceProviderCredentials.appuserID = userID
        serviceProviderCredentials.clientID = clientID
        serviceProviderCredentials.serviceProviderType = ServiceProviderType.EMAIL_SERVICE_PROVIDER.desc
        //check credential are correct or not
        //println("checking connection")
        val success = userSettingsService.testConnection(serviceProviderCredentials)
        //println(success)
        return if (success) {
            userSettingsService.saveEmailServiceProvider(serviceProviderCredentials, Status.ACTIVE)
            //clientID?.let{ id->campaignService.resumeAllForcePaused(id)}
        } else 0L
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
        val clientID = AuthenticationUtils.clientID?: throw AccessDeniedException("")
        val userID = AuthenticationUtils.principal.id
        serviceProviderCredentials.appuserID = userID
        serviceProviderCredentials.clientID = clientID
        serviceProviderCredentials.serviceProviderType = ServiceProviderType.SMS_SERVICE_PROVIDER.desc
        return userSettingsService.saveSmsServiceProvider(serviceProviderCredentials)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/notification-service-providers"])
    fun getNotificationServiceProviders(): List<ServiceProviderCredentials> {
        val clientID = AuthenticationUtils.clientID
        return userSettingsService.getNotificationServiceProvider(clientID!!)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/notification-service-provider/{id}"])
    fun getNotificationServiceProvider(@PathVariable id: Long): ServiceProviderCredentials? {
        val clientID = AuthenticationUtils.clientID
        return userSettingsService.getNotificationServiceProvider(clientID!!, id)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = ["/notification-service-provider/save"])
    fun saveNotificationServiceProvider(@Valid @RequestBody serviceProviderCredentials: ServiceProviderCredentials): Long? {
        val clientID = AuthenticationUtils.clientID
        val userID = AuthenticationUtils.principal.id
        serviceProviderCredentials.appuserID = userID
        serviceProviderCredentials.clientID = clientID
        serviceProviderCredentials.serviceProviderType = ServiceProviderType.NOTIFICATION_SERVICE_PROVIDER.desc
        return userSettingsService.saveNotificationServiceProvider(serviceProviderCredentials)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = ["/android-push-service-provider/save"])
    fun saveAndroidPushServiceProvider(@Valid @RequestBody serviceProviderCredentials: ServiceProviderCredentials): Long? {
        val clientID = AuthenticationUtils.clientID
        val userID = AuthenticationUtils.principal.id
        serviceProviderCredentials.appuserID = userID
        serviceProviderCredentials.clientID = clientID
        serviceProviderCredentials.serviceProviderType = ServiceProviderType.ANDROID_PUSH_SERVICE_PROVIDER.desc
        return userSettingsService.saveAndroidPushServiceProvider(serviceProviderCredentials)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = ["/web-push-service-provider/save"])
    fun saveWebPushServiceProvider(@Valid @RequestBody serviceProviderCredentials: ServiceProviderCredentials): Long? {
        val clientID = AuthenticationUtils.clientID
        val userID = AuthenticationUtils.principal.id
        serviceProviderCredentials.appuserID = userID
        serviceProviderCredentials.clientID = clientID
        serviceProviderCredentials.serviceProviderType = ServiceProviderType.WEB_PUSH_SERVICE_PROVIDER.desc
        return userSettingsService.saveWebPushServiceProvider(serviceProviderCredentials)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = ["/ios-push-service-provider/save"])
    fun saveIOSPushServiceProvider(@Valid @RequestBody serviceProviderCredentials: ServiceProviderCredentials): Long? {
        val clientID = AuthenticationUtils.clientID
        val userID = AuthenticationUtils.principal.id
        serviceProviderCredentials.appuserID = userID
        serviceProviderCredentials.clientID = clientID
        serviceProviderCredentials.serviceProviderType = ServiceProviderType.IOS_PUSH_SERVICE_PROVIDER.desc
        return userSettingsService.saveIOSPushServiceProvider(serviceProviderCredentials)
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = ["/senders-email/add"])
    fun addSendersEmail(@RequestBody email: EmailAddress) {
        val clientID = AuthenticationUtils.clientID?: throw AccessDeniedException("")
        userSettingsService.addSenderEmailAddress(email, clientID)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/senders-email/list"])
    fun getSendersEmailList(@RequestParam(value = "verified", required = false) verified: Boolean?): List<EmailAddress> {

        val onlyVerified = verified ?: true;
        val clientID = AuthenticationUtils.clientID
        return userSettingsService.getSenderEmailAddresses(clientID!!, onlyVerified)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = ["/senders-email/delete"])
    fun deleteSendersEmail(@RequestBody email: EmailAddress) {
        val clientID = AuthenticationUtils.clientID
        userSettingsService.removeSenderEmailAddress(email, clientID!!)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = ["/account-settings/save"])
    fun saveAccountSettings(@RequestBody accountSettings: AccountSettings):Map<String,Any>? {
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
        return if (linkOptional.isPresent) linkOptional.get() else null
    }

    @GetMapping(value = ["/verifyemail"])
    fun verifyEmail(@RequestParam(value = "c") link: String) {

        var decodeString = URLDecoder.decode(link, "UTF-8")
        var decryptString = decrypt(decodeString)
        var details = decryptString.split("||")
        var timeStamp = details[0].toLong()
        var mail = details[1]
        var clientId = details[2].toLong()

        userSettingsService.updateStatusOfEmailSetting(timeStamp, mail, clientId)


    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = arrayOf("/mark/default"))
    fun markDefault(@RequestParam(required = true,name = "default") default:Boolean,
                    @RequestParam(required = true,name = "type") type:String,
                    @RequestParam(required = true,name = "id") id:Long):ResponseEntity<String>{
        try {
            userSettingsService.markDefault(type,id,default)
            return ResponseEntity(HttpStatus.OK)
        }catch (ex:CustomException){
            return ResponseEntity(ex.message?:"",HttpStatus.BAD_REQUEST)
        }
    }

//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    @PostMapping("app/account-settings/save")
//    fun saveAppAccountSettings(@Valid @RequestBody settings:AccountSettings){
//
//    }

}

