package com.und.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.common.utils.BuildCampaignMessage
import com.und.config.EventStream
import com.und.model.jpa.*
import com.und.model.mongo.EventUser
import com.und.model.utils.Email
import com.und.model.utils.FcmMessage
import com.und.model.utils.Sms
import com.und.repository.jpa.*
import com.und.utils.loggerFor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.Modifying
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
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

    @Autowired
    private lateinit var buildCampaignMessage:BuildCampaignMessage

    fun executeCampaign(campaignId: Long, clientId: Long) {
//        val campaignOption = campaignRepository.getCampaignByCampaignId(campaignId, clientId)
        val campaign = findCampaign(campaignId, clientId)
        val usersData = getUsersData(campaign.segmentationID!!, clientId,campaign.campaignType)
        usersData.forEach { user ->
            executeCampaignForUser(campaign, user, clientId)
        }
    }

    fun executeLiveCampaign(campaign: Campaign, clientId: Long, user: EventUser) {
        val present=communicationChannelPresent(campaign,user)
        if(present) executeCampaignForUser(campaign, user, clientId)
    }

    private fun communicationChannelPresent(campaign: Campaign,user: EventUser):Boolean{
        return when (campaign.campaignType) {
            "EMAIL" -> {
                user.identity.email != null
            }
            "SMS" -> {
                user.identity.mobile != null
            }
            "PUSH_ANDROID" -> {
                user.identity.androidFcmToken != null
            }
            "PUSH_WEB" -> {
                user.identity.webFcmToken != null
            }
            "PUSH_IOS" -> {
                user.identity.iosFcmToken != null
            }
            else -> false
        }
    }
    private fun findCampaign(campaignId: Long, clientId: Long): Campaign {
        val campaignOption = campaignRepository.findById(campaignId)
        return campaignOption.orElseThrow { IllegalStateException("campaign not found for campaign id $campaignId and client $clientId") }
    }

    fun findLiveSegmentCampaign(segmentId: Long, clientId: Long): List<Campaign> {
        //FIXME if client panel and email send service are running in diff timezone then there is exact time matching problem.
        return  campaignRepository.getCampaignByClientIDAndSegmentationIDAndStartDateBeforeAndEndDateAfter(segmentId, clientId)
    }

    fun findAllLiveSegmentCampaignBySegmentId(segmentId: Long, clientId: Long): List<Campaign> {
        //FIXME if client panel and email send service are running in diff timezone then there is exact time matching problem.
        return  campaignRepository.findAllByClientIDAndSegmentationIDAndStartDateBefore(segmentId, clientId)
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
        return buildCampaignMessage.buildSms(clientId, campaign, user, smsCampaign.get(), smsTemplate.get())
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
            return buildCampaignMessage.buildEmail(clientId, campaign, user, emailCampaign.get(), emailTemplate.get())
        }catch (ex:Exception){
            throw ex
        }
    }



    private fun fcmAndroidMessage(clientId: Long,campaign: Campaign,user: EventUser):FcmMessage{
        //Todo passing data model
        val androidCampaign =androidCampaignRepository.findByCampaignId(campaign.id!!)
        if (!androidCampaign.isPresent) throw Exception("Android Campaign not exist for clientId ${clientId} and campaignId ${campaign.id}")
        return buildCampaignMessage.buildAndroidFcmMessage(clientId, androidCampaign.get(), user, campaign)
    }


    private fun fcmWebMessage(clientId: Long,campaign: Campaign,user: EventUser,token:String):FcmMessage{
        val webPushCampaign =webCampaignRepository.findByCampaignId(campaign.id!!)
        if (!webPushCampaign.isPresent) throw Exception("Web Campaign not exist for clientId ${clientId} and campaignId ${campaign.id}")
        return buildCampaignMessage.buildWebFcmMessage(clientId, webPushCampaign.get(), token, campaign, user)
    }



    private fun fcmIosMessage(clientId: Long,campaign: Campaign,user: EventUser):FcmMessage{
//        val iosCampaign =iosCampaignRepository.findByCampaignId(campaign.id!!)
        return buildCampaignMessage.buildIosFcmMessage(clientId, user, campaign)
    }



    fun updateCampaignStatus(status:CampaignStatus,clientId: Long,segmentId: Long){
        campaignRepository.updateStatusOfCampaign(status.name,segmentId,clientId)
    }
    fun updateCampaignStatusByCampaignId(status:CampaignStatus,clientId: Long,campaignId: Long){
        campaignRepository.updateStatusOfCampaign(status.name,campaignId,clientId)
    }
    fun getUsersData(segmentId: Long, clientId: Long,campaignType:String): List<EventUser> {
        val segment = segmentService.getWebSegment(segmentId, clientId)
        return segmentService.getUserData(segment, clientId,campaignType)
    }
    fun toKafka(fcmMessage: FcmMessage){
            eventStream.fcmEventSend().send(MessageBuilder.withPayload(fcmMessage).build())
    }
    fun toKafka(email: Email): Boolean =
            eventStream.emailEventSend().send(MessageBuilder.withPayload(email).build())


    fun toKafka(sms: Sms): Boolean =
            eventStream.smsEventSend().send(MessageBuilder.withPayload(sms).build())


}