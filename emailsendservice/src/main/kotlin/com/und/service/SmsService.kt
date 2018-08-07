package com.und.service

import com.und.model.mongo.SmsStatus
import com.und.model.utils.ServiceProviderCredentials
import com.und.model.utils.Sms
import com.und.repository.jpa.ClientSettingsRepository
import com.und.utils.loggerFor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service


@Service
class SmsService {

    companion object {
        protected val logger = loggerFor(EmailService::class.java)
    }


    @Autowired
    private lateinit var serviceProviderCredentialsService: ServiceProviderCredentialsService

    @Autowired
    private lateinit var clientSettingsRepository: ClientSettingsRepository

    @Autowired
    private lateinit var smsHelperService: SmsHelperService


    private var wspCredsMap: MutableMap<Long, ServiceProviderCredentials> = mutableMapOf()


     fun sendSms(sms: Sms){
        val smsToSend = smsHelperService.updateBody(sms)
        val mongoSmsId = smsHelperService.saveSmsInMongo(smsToSend, SmsStatus.NOT_SENT)
        //FIXME: cache the findByClientID clientSettings
        //val clientSettings = clientSettingsRepository.findByClientID(smsToSend.clientID)
        sendSmsWithoutTracking(smsToSend)
        smsHelperService.updateSmsStatus(mongoSmsId, SmsStatus.SENT, smsToSend.clientID)
    }

    fun sendSmsWithoutTracking(sms: Sms){
            val serviceProviderCredential = serviceProviderCredentials(sms)
            serviceProviderCredential.sendSms(sms)

    }

    @Cacheable
    private fun serviceProviderCredentials(sms: Sms): ServiceProviderCredentials {
        synchronized(sms.clientID) {
            //TODO: This code can be cached in Redis
            if (!wspCredsMap.containsKey(sms.clientID)) {
                val webServiceProviderCred = serviceProviderCredentialsService.getServiceProviderCredentials(sms.clientID)
                wspCredsMap[sms.clientID] = webServiceProviderCred
            }
        }
        return wspCredsMap[sms.clientID]!!
    }
}

fun ServiceProviderCredentials.sendSms(sms:Sms){
    when(this.serviceProvider){
        ServiceProviderCredentialsService.ServiceProvider.Twillio.desc->
            TwilioSmsSendService().sendSms(this,sms)
        ServiceProviderCredentialsService.ServiceProvider.AWS_SNS.desc->
            AWS_SNSSmsService().sendSms(this,sms)
    }
}
