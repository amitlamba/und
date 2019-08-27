package com.und.sms.utility

import com.und.email.service.EmailService
import com.und.model.utils.ServiceProviderCredentials
import com.und.model.utils.Sms
import com.und.report.service.AWSSmsLambdaInvoker
import com.und.report.service.Response
import com.und.report.service.SmsData
import com.und.report.service.TwillioSmsData
import com.und.repository.jpa.security.UserRepository
import com.und.service.*
import com.und.sms.service.SmsHelperService
import com.und.sms.service.SmsSendService
import com.und.utils.loggerFor
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SmsServiceUtility {

    companion object {
        protected val logger: Logger = loggerFor(SmsServiceUtility::class.java)
    }


    @Autowired
    private lateinit var serviceProviderCredentialsService: ServiceProviderCredentialsService

//    @Autowired
//    private lateinit var smsHelperService: SmsHelperService

    @Autowired
    private lateinit var smsLambdaInvoker: AWSSmsLambdaInvoker

    @Autowired
    private lateinit var smsSendService:SmsSendService

    private var wspCredsMap: MutableMap<Long, ServiceProviderCredentials> = mutableMapOf()

//    @Autowired
//    private lateinit var userRepository: UserRepository
//    @Autowired
//    private lateinit var eventApiFeignClient: EventApiFeignClient

    fun sendSmsWithoutTracking(sms: Sms): Response {
        logger.info("sending sms without tracking")
        val serviceProviderCredential = serviceProviderCredentials(sms)
        logger.info("sending sms without tracking complete")
        return try {
            when(serviceProviderCredential.serviceProvider){
                ServiceProviderCredentialsService.ServiceProvider.Twillio.desc -> {
                    val smsData = buildTwillioSmsData(serviceProviderCredential,sms)
                    val response  = smsSendService.sendTwillioSms(smsData)
                    return response
                }
                else -> {
                    val smsData = buildSmsData(sms, serviceProviderCredential)
                    val response = smsLambdaInvoker.sendSms(smsData)
                    return response
                }
            }
        } catch (e: Exception) {
            logger.error("Couldn't build smsData to invoke sms api", e)
            Response(400, "invalid input values for sms ${e.printStackTrace()}")
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

    fun buildTwillioSmsData(serviceProviderCredentials: ServiceProviderCredentials,sms: Sms):TwillioSmsData{
        val username = serviceProviderCredentials.credentialsMap["username"]
        val password = serviceProviderCredentials.credentialsMap["password"]
        val fromUser = serviceProviderCredentials.credentialsMap["fromUser"]
        logger.info("Username is $username toaddress is ${sms.toSmsAddresses} body is ${sms.smsBody}")
        return when{
            !(username.isNullOrBlank() || password.isNullOrBlank()) -> {
                 TwillioSmsData(username!!,
                        password!!,
                        sms.toSmsAddresses!!,
                        fromUser!!,
                        sms.smsBody!!)
            }
            else -> {
                throw Exception("Not implemented")
            }
        }
    }
}