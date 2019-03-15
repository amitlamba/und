package com.und.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.config.EventStream
import com.und.model.jpa.Campaign
import com.und.model.mongo.EventUser
import com.und.model.utils.Email
import com.und.model.utils.FcmMessage
import com.und.model.utils.Sms
import com.und.repository.jpa.*
import com.und.utils.loggerFor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import javax.mail.internet.InternetAddress

@Service
class CampaignService {

    companion object {
        protected val logger = loggerFor(CampaignService::class.java)
    }

    @Autowired
    private lateinit var campaignRepository: CampaignRepository
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    @Autowired
    private lateinit var segmentService: SegmentService
    @Autowired
    private lateinit var emailCampaignRepository: EmailCampaignRepository
    @Autowired
    private lateinit var androidCampaignRepository: AndroidCampaignRepository
    @Autowired
    private lateinit var webCampaignRepository: WebPushCampaignRepository
    @Autowired
    private lateinit var smsCampaignRepository: SmsCampaignRepository
    @Autowired
    private lateinit var clientEmailSettingsRepository: ClientEmailSettingsRepository
    @Autowired
    private lateinit var emailTemplateRepository: EmailTemplateRepository
    @Autowired
    private lateinit var eventStream: EventStream

    fun executeCampaign(campaignId: Long, clientId: Long) {
//        val campaignOption = campaignRepository.getCampaignByCampaignId(campaignId, clientId)
        val campaign = findCampaign(campaignId, clientId)
        val usersData = getUsersData(campaign.segmentationID!!, clientId)
        usersData.forEach { user ->
            executeCampaignForUser(campaign, user, clientId)
        }
    }

    fun executeLiveCampaign(campaign: Campaign, clientId: Long, user: EventUser) {
            executeCampaignForUser(campaign, user, clientId)
    }

    private fun findCampaign(campaignId: Long, clientId: Long): Campaign {
        val campaignOption = campaignRepository.findById(campaignId)
        return campaignOption.orElseThrow { IllegalStateException("campaign not found for campaign id $campaignId and client $clientId") }
    }

    fun findLiveSegmentCampaign(segmentId: Long, clientId: Long): List<Campaign> {
        //FIXME if client panel and email send service are running in diff timezone then there is exact time matching problem.
        val time=LocalDateTime.now()
        return  campaignRepository.getCampaignByClientIDAndSegmentationIDAndEndDateAfter(segmentId, clientId,time)
    }

    private fun executeCampaignForUser(campaign: Campaign, user: EventUser, clientId: Long) {
        try {
            //TODO: filter out unsubscribed and blacklisted users
            //TODO: How to skip transactional Messages
            //check mode of communication is email
            if (campaign.campaignType == "EMAIL") {

                if (user.communication?.email?.dnd == true)
                    return //Local lambda return
                val email: Email = email(clientId, campaign, user)
                toKafka(email)
            }
            //check mode of communication is sms
            if (campaign.campaignType == "SMS") {

                if (user.communication?.mobile?.dnd == true)
                    return //Local lambda return
                val sms: Sms = sms(clientId, campaign, user)
                toKafka(sms)
            }
    //                check mode of communication is mobile push
            if (campaign.campaignType == "PUSH_ANDROID") {
                if (user.communication?.android?.dnd == true)
                    return //Local lambda return
                val notification = fcmAndroidMessage(clientId, campaign, user)
                toKafka(notification)
            }
            if (campaign.campaignType == "PUSH_WEB") {
                if (user.communication?.webpush?.dnd == true)
                    return //Local lambda return
                user.identity.webFcmToken?.forEach {
                    val notification = fcmWebMessage(clientId, campaign, user, it)
                    toKafka(notification)
                }
            }
            if (campaign.campaignType == "PUSH_IOS") {
                if (user.communication?.ios?.dnd == true)
                    return //Local lambda return
                val notification = fcmIosMessage(clientId, campaign, user)
                toKafka(notification)
            }
        } catch (ex: Exception) {
            logger.error(ex.message)

        } finally {

        }
    }


    private fun sms(clientId: Long, campaign: Campaign, user: EventUser): Sms {
        val smsCampaign=smsCampaignRepository.findByCampaignId(campaign.id!!)
        if (!smsCampaign.isPresent) throw Exception("Sms Campaign not exist for clientId ${clientId} and campaignId ${campaign.id}")
        val smsTemplate=emailTemplateRepository.findByIdAndClientID(smsCampaign.get().templateId!!,clientId)
        if(!smsTemplate.isPresent) throw Exception("Sms Template for clientId ${clientId} , templateId ${smsCampaign.get().templateId} not exists.")
        return Sms(
                clientId,
                campaign.fromUser,
                user.identity.mobile,
                smsBody = null,
                smsTemplateId = smsCampaign.get().templateId ?: 0L,
                smsTemplateName = smsTemplate.get().name,
                eventUser = user,
                serviceProviderId = campaign.serviceProviderId,
                campaignId = campaign.id,
                segmentId = campaign.segmentationID
        )
    }

    private fun email(clientId: Long, campaign: Campaign, user: EventUser): Email {
        try {
            val emailCampaign = emailCampaignRepository.findByCampaignId(campaign.id!!)
            if (!emailCampaign.isPresent) throw Exception("Email Campaign not exist for clientId ${clientId} and campaignId ${campaign.id}")
            val emailTemplate = emailTemplateRepository.findByIdAndClientID(emailCampaign.get().templateId!!, clientId)
            if (!emailTemplate.isPresent) throw Exception("Email Template for clientId ${clientId} , templateId ${emailCampaign.get().templateId} not exists.")
//        val clientEmailSettings= clientEmailSettingsRepository.
//                findByClientIdAndEmailAndServiceProviderId(clientId,campaign.fromUser!!,campaign.serviceProviderId!!)
//        if (!clientEmailSettings.isPresent) throw Exception("Client Email Settings not present for client ${clientId} fromAddress ${campaign.fromUser} sp ${campaign.serviceProviderId}")
            return Email(
                    clientID = clientId,
                    fromEmailAddress = InternetAddress.parse(campaign.fromUser, false)[0],
                    toEmailAddresses = InternetAddress.parse(user.identity.email, false),
                    emailTemplateId = emailCampaign.get().templateId ?: 0L,
                    emailTemplateName = emailTemplate.get().name,
                    campaignId = campaign.id!!,
                    eventUser = user,
                    clientEmailSettingId = emailCampaign.get().clientSettingEmailId,
                    segmentId = campaign.segmentationID
            )
        }catch (ex:Exception){
            throw ex
        }
    }
    private fun fcmAndroidMessage(clientId: Long,campaign: Campaign,user: EventUser):FcmMessage{
        //Todo passing data model
        val androidCampaign =androidCampaignRepository.findByCampaignId(campaign.id!!)
        if (!androidCampaign.isPresent) throw Exception("Android Campaign not exist for clientId ${clientId} and campaignId ${campaign.id}")
        return FcmMessage(
                clientId=clientId,
                templateId = androidCampaign.get().templateId?:0L,
                to = user.identity.androidFcmToken?:"",
                type = "android",
                campaignId = campaign.id!!,
                userId = user.id,
                eventUser = user,
                serviceProviderId = campaign.serviceProviderId,
                segmentId = campaign.segmentationID
        )
    }

    private fun fcmWebMessage(clientId: Long,campaign: Campaign,user: EventUser,token:String):FcmMessage{
        val webPushCampaign =webCampaignRepository.findByCampaignId(campaign.id!!)
        if (!webPushCampaign.isPresent) throw Exception("Web Campaign not exist for clientId ${clientId} and campaignId ${campaign.id}")
        return FcmMessage(
                clientId = clientId,
                templateId = webPushCampaign.get().templateId?:0L,
                to = token,
                type = "web",
                campaignId = campaign.id!!,
                userId = user.id,
                eventUser = user,
                serviceProviderId = campaign.serviceProviderId,
                segmentId = campaign.segmentationID
        )
    }
    private fun fcmIosMessage(clientId: Long,campaign: Campaign,user: EventUser):FcmMessage{
//        val iosCampaign =iosCampaignRepository.findByCampaignId(campaign.id!!)
        return FcmMessage(
                clientId = clientId,
//                templateId = iosCampaign.get().templateId?:0L,
                templateId = 0L,
                to = user.identity.iosFcmToken?:"",
                type = "ios",
                campaignId = campaign.id!!,
                userId = user.id,
                eventUser = user,
                serviceProviderId = campaign.serviceProviderId,
                segmentId = campaign.segmentationID
        )
    }

    fun getUsersData(segmentId: Long, clientId: Long): List<EventUser> {
        val segment = segmentService.getWebSegment(segmentId, clientId)
        return segmentService.getUserData(segment, clientId)
    }
    fun toKafka(fcmMessage: FcmMessage){
            eventStream.fcmEventSend().send(MessageBuilder.withPayload(fcmMessage).build())
    }
    fun toKafka(email: Email): Boolean =
            eventStream.emailEventSend().send(MessageBuilder.withPayload(email).build())


    fun toKafka(sms: Sms): Boolean =
            eventStream.smsEventSend().send(MessageBuilder.withPayload(sms).build())


}