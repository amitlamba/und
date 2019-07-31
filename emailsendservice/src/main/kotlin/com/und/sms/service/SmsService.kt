package com.und.sms.service

import com.und.campaign.repository.jpa.CampaignRepository
import com.und.campaign.repository.jpa.EmailCampaignRepository
import com.und.campaign.repository.jpa.SmsCampaignRepository
import com.und.campaign.repository.mongo.EventUserRepository
import com.und.common.utils.BuildCampaignMessage
import com.und.sms.utility.SmsServiceUtility
import com.und.email.service.EmailService
import com.und.model.mongo.SmsStatus
import com.und.model.utils.LiveCampaignTriggerInfo
import com.und.model.utils.Sms
import com.und.model.utils.eventapi.Identity
import com.und.repository.jpa.security.UserRepository
import com.und.service.CommonSmsService
import com.und.service.EventApiFeignClient
import com.und.sms.repository.jpa.SmsTemplateRepository
import com.und.utils.loggerFor
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class SmsService : CommonSmsService {

    companion object {
        protected val logger: Logger = loggerFor(EmailService::class.java)
    }

    @Autowired
    private lateinit var smsHelperService: SmsHelperService

    @Autowired
    private lateinit var buildCampaignMessage: BuildCampaignMessage

    @Autowired
    private lateinit var campaignRepository: CampaignRepository

    @Autowired
    private lateinit var smsCampaignRepository: SmsCampaignRepository

    @Autowired
    private lateinit var smsTemplateRepository : SmsTemplateRepository

    @Autowired
    private lateinit var eventUserRepository: EventUserRepository


//    @Autowired
//    private lateinit var smsLambdaInvoker: AWSSmsLambdaInvoker
//
//    private var wspCredsMap: MutableMap<Long, ServiceProviderCredentials> = mutableMapOf()

    @Autowired
    private lateinit var userRepository:UserRepository
    @Autowired
    private lateinit var eventApiFeignClient: EventApiFeignClient
    @Autowired
    private lateinit var smsServiceUtility: SmsServiceUtility

    fun sendLiveSms(infoModel:LiveCampaignTriggerInfo){
        val campaign = campaignRepository.findById(infoModel.campaignId).get()
        val smsCampaign = smsCampaignRepository.findByCampaignId(infoModel.campaignId).get()
        val smsTemplate = infoModel.templateId?.let {  smsTemplateRepository.findByIdAndClientID(it,infoModel.clientId)}
        ?:smsTemplateRepository.findByIdAndClientID(smsCampaign.templateId!!,infoModel.clientId)
        val user = eventUserRepository.findByIdAndClientId(ObjectId(infoModel.userId),infoModel.clientId)
        user?.let {
           smsTemplate?.let {  val sms = buildCampaignMessage.buildSms(infoModel.clientId,campaign,user,smsCampaign,smsTemplate)
               sendSms(sms) }
        }
    }

    override fun sendSms(sms: Sms) {
        val mongoSmsId = ObjectId().toString()
        val smsToSend = smsHelperService.updateBody(sms)

        smsHelperService.saveSmsInMongo(smsToSend, SmsStatus.NOT_SENT, mongoSmsId)
        //FIXME: cache the findByClientID clientSettings
        //val clientSettings = clientSettingsRepository.findByClientID(smsToSend.clientID)
        val response = smsServiceUtility.sendSmsWithoutTracking(smsToSend)
        if (response.status == 200) {
            smsHelperService.updateSmsStatus(mongoSmsId, SmsStatus.SENT, smsToSend.clientID, response.message)

            val token = userRepository.findSystemUser().key
            var event= com.und.model.utils.eventapi.Event()
            with(event) {
                name = "Notification Sent"
                clientId=smsToSend.clientID
                notificationId=mongoSmsId
                attributes.put("campaign_id",sms.campaignId?:-1)
                attributes.put("template_id",sms.smsTemplateId)
                userIdentified=true
                identity= Identity(userId = sms.eventUser?.id,clientId = smsToSend.clientID.toInt(),idf = 1)

            }
            eventApiFeignClient.pushEvent(token,event)

        } else {
            smsHelperService.updateSmsStatus(mongoSmsId, SmsStatus.ERROR, smsToSend.clientID, response.message)
            throw Exception("400, invalid input values for sms")
        }
    }

//    fun sendSmsWithoutTracking(sms: Sms): Response {
//        val serviceProviderCredential = serviceProviderCredentials(sms)
//        return try {
//            val smsData = buildSmsData(sms, serviceProviderCredential)
//            val response = smsLambdaInvoker.sendSms(smsData)
//            return response
//        } catch (e: Exception) {
//            logger.error("Couldn't build smsData to invoke sms api", e)
//            Response(400, "invalid input values for sms")
//        }
//
//    }
//
//    fun serviceProviderCredentials(sms: Sms): ServiceProviderCredentials {
//        synchronized(sms.clientID) {
//            //TODO: This code can be cached in Redis
//            if (!wspCredsMap.containsKey(sms.clientID)) {
//                val webServiceProviderCred = serviceProviderCredentialsService.getServiceProviderCredentials(sms.clientID,sms.serviceProviderId)
//                wspCredsMap[sms.clientID] = webServiceProviderCred
//            }
//        }
//        return wspCredsMap[sms.clientID]!!
//    }
//}
//
//fun buildSmsData(sms: Sms, serviceProviderCredentials: ServiceProviderCredentials): SmsData {
//    val credential = serviceProviderCredentials
//    return when (credential.serviceProvider) {
//        ServiceProviderCredentialsService.ServiceProvider.Exotel.desc -> {
//            //val serviceProviderType = credential.serviceProviderType
//            val sid = credential.credentialsMap["sid"]
//            val accessToken = credential.credentialsMap["token"]
//
//            SmsData(
//                    from = sms.fromSmsAddress!!,
//                    to = sms.toSmsAddresses!!,
//                    body = sms.smsBody!!,
//                    token = accessToken!!,
//                    sid = sid!!
//            )
//
//        }
//        else -> throw Exception("Not implemented")
//    }

}
