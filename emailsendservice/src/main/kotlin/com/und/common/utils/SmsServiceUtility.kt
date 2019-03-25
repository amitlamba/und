package com.und.common.utils

import com.und.model.utils.ServiceProviderCredentials
import com.und.model.utils.Sms
import com.und.report.service.AWSSmsLambdaInvoker
import com.und.report.service.Response
import com.und.report.service.SmsData
import com.und.repository.jpa.security.UserRepository
import com.und.service.*
import com.und.utils.loggerFor
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SmsServiceUtility {

    companion object {
        protected val logger: Logger = loggerFor(EmailService::class.java)
    }


    @Autowired
    private lateinit var serviceProviderCredentialsService: ServiceProviderCredentialsService

    @Autowired
    private lateinit var smsHelperService: SmsHelperService

    @Autowired
    private lateinit var smsLambdaInvoker: AWSSmsLambdaInvoker

    private var wspCredsMap: MutableMap<Long, ServiceProviderCredentials> = mutableMapOf()

    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var eventApiFeignClient: EventApiFeignClient

    fun sendSmsWithoutTracking(sms: Sms): Response {
        val serviceProviderCredential = serviceProviderCredentials(sms)
        return try {
            val smsData = buildSmsData(sms, serviceProviderCredential)
            val response = smsLambdaInvoker.sendSms(smsData)
            return response
        } catch (e: Exception) {
            logger.error("Couldn't build smsData to invoke sms api", e)
            Response(400, "invalid input values for sms")
        }

    }

    fun serviceProviderCredentials(sms: Sms): ServiceProviderCredentials {
        synchronized(sms.clientID) {
            //TODO: This code can be cached in Redis
            if (!wspCredsMap.containsKey(sms.clientID)) {
                val webServiceProviderCred = serviceProviderCredentialsService.getServiceProviderCredentials(sms.clientID, sms.serviceProviderId)
                wspCredsMap[sms.clientID] = webServiceProviderCred
            }
        }
        return wspCredsMap[sms.clientID]!!
    }

    fun buildSmsData(sms: Sms, serviceProviderCredentials: ServiceProviderCredentials): SmsData {
        val credential = serviceProviderCredentials
        return when (credential.serviceProvider) {
            ServiceProviderCredentialsService.ServiceProvider.Exotel.desc -> {
                //val serviceProviderType = credential.serviceProviderType
                val sid = credential.credentialsMap["sid"]
                val accessToken = credential.credentialsMap["token"]

                SmsData(
                        from = sms.fromSmsAddress!!,
                        to = sms.toSmsAddresses!!,
                        body = sms.smsBody!!,
                        token = accessToken!!,
                        sid = sid!!
                )

            }
            else -> throw Exception("Not implemented")
        }
    }
}