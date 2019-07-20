package com.und.fcmpush.listner

import com.und.campaign.model.CampaignUserStatus
import com.und.campaign.model.CampaignUsers
import com.und.campaign.repository.jpa.AndroidCampaignRepository
import com.und.campaign.repository.jpa.CampaignRepository
import com.und.campaign.repository.jpa.WebPushCampaignRepository
import com.und.campaign.repository.mongo.CampaignUsersRepository
import com.und.campaign.repository.mongo.EventUserRepository
import com.und.common.utils.BuildCampaignMessage
import com.und.config.EventStream
import com.und.exception.FcmFailureException
import com.und.model.utils.FcmMessage
import com.und.fcmpush.service.FcmService
import com.und.fcmpush.service.NotificationError
import com.und.model.jpa.Campaign
import com.und.model.mongo.EventUser
import com.und.model.utils.CampaignType
import com.und.model.utils.LiveCampaignTriggerInfo
import com.und.repository.jpa.CampaignTriggerInfoRepository
import com.und.sms.listner.GroupStatus
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class FcmListner {

    @Autowired
    private lateinit var fcmSendService: FcmService

    @Autowired
    @Qualifier("testfcmservice")
    lateinit var fcmService : FcmService

    @Autowired
    private lateinit var buildCampaignMessage: BuildCampaignMessage

    @Autowired
    private lateinit var eventUserRepository: EventUserRepository
    @Autowired
    private lateinit var campaignRepository: CampaignRepository
    @Autowired
    private lateinit var androidCampaignRepository: AndroidCampaignRepository
    @Autowired
    private lateinit var webCampaignRepository: WebPushCampaignRepository
    @Autowired
    private lateinit var campaignUsersRepository: CampaignUsersRepository
    @Autowired
    private lateinit var campaignTriggerInfoRepository: CampaignTriggerInfoRepository

    @Autowired
    private lateinit var eventStream:EventStream

    //TODO make reties 0
    @StreamListener("fcmEventReceive")
    fun sendMessage(campaignUsers: CampaignUsers) {
        //check error in campaign triggerinfo
        val campaignTriggerInfo = campaignTriggerInfoRepository.findById(campaignUsers.campaignId)
        if(campaignTriggerInfo.isPresent && !campaignTriggerInfo.get().error) {
            val clientId = campaignUsers.clientId
            val campaignId = campaignUsers.campaignId
            val templateId = campaignUsers.templateId
            val campaign = campaignRepository.getCampaignByCampaignId(campaignId, clientId).get()
            when (campaignUsers.groupStatus) {
                GroupStatus.ERROR -> {
                    campaignUsers.users.forEachIndexed { index, value ->
                        val status = value["status"]
                        val userId = value["userId"] as String
                        val eventUser = eventUserRepository.findByIdAndClientId(ObjectId(userId), clientId)
                        eventUser?.let {
                            when (status) {
                                CampaignUserStatus.UNDELIVERED -> {
                                    try {
                                        sendCampaignMessage(campaign, templateId, it)
                                        updateCampaignUserDocument(null, campaignUsers)
                                    } catch (ex: FcmFailureException) {
                                        campaignTriggerInfoRepository.updateErrorStatus(campaignId,true)
                                        updateCampaignUserDocument(index, campaignUsers)
                                        toFcmFailureKafka(ex.error)
                                        return@forEachIndexed
                                    }
                                }
                                else -> {

                                }
                            }
                        }

                    }
                }
                GroupStatus.UNDELIVERED -> {
                    campaignUsers.users.forEachIndexed { index, value ->
                        val userId = value["userId"] as String
                        val eventUser = eventUserRepository.findByIdAndClientId(ObjectId(userId), clientId)
                        eventUser?.let {
                            try {
                                sendCampaignMessage(campaign, templateId, it)
                                updateCampaignUserDocument(null, campaignUsers)
                            } catch (ex: FcmFailureException) {
                                campaignTriggerInfoRepository.updateErrorStatus(campaignId,true)
                                updateCampaignUserDocument(index, campaignUsers)
                                toFcmFailureKafka(ex.error)
                                return@forEachIndexed
                            }
                        }
                    }
                }
            }
        }
    }
    fun updateCampaignUserDocument(errorPosition:Int?,campaignUsers: CampaignUsers){
        errorPosition?.let {
            val users = campaignUsers.users.mapIndexed { index, document ->
                if(index>=errorPosition){
                    Document(mapOf(Pair("userId",document["userId"]),Pair("status", CampaignUserStatus.UNDELIVERED)))
                }else{
                    Document(mapOf(Pair("userId",document["userId"]),Pair("status", CampaignUserStatus.DELIVERED)))
                }
            }
            campaignUsers.groupStatus = GroupStatus.ERROR
            campaignUsers.users = users
        }
        if(errorPosition == null) campaignUsers.groupStatus = GroupStatus.DELIVERED
        //make it client specific
        campaignUsers.deliveryTime = LocalDateTime.now(ZoneId.systemDefault())
        campaignUsersRepository.save(campaignUsers)
    }

    fun sendCampaignMessage(campaign: Campaign, templateId:Long, eventUser: EventUser){
        when(campaign.campaignType){
            CampaignType.PUSH_ANDROID.name ->{
                val androidCampaign = androidCampaignRepository.findByCampaignId(campaign.id!!).get()
                val message = buildCampaignMessage.buildAndroidFcmMessage(campaign.clientID!!,androidCampaign,eventUser,campaign,templateId)
                fcmSendService.sendMessage(message)
            }
            CampaignType.PUSH_WEB.name ->{
                val webCampaign = webCampaignRepository.findByCampaignId(campaign.id!!).get()
                eventUser.identity.webFcmToken?.forEach {
                    val message = buildCampaignMessage.buildWebFcmMessage(campaign.clientID!!,webCampaign,it,campaign,eventUser,templateId)
                    fcmSendService.sendMessage(message)
                }
            }else -> throw FcmFailureException("campaign type is wrong (${campaign.campaignType}) expected web or android only")
        }
    }

    fun toFcmFailureKafka(notificationError: NotificationError) {
        eventStream.fcmFailureEventSend().send(MessageBuilder.withPayload(notificationError).build())
    }
    @StreamListener("inTestFcm")
    fun sendTestFcmMessage(message: FcmMessage){
        fcmService.sendMessage(message)
    }

    @StreamListener("inFcmLiveCampaign")
    fun inFcmLiveCampaign(infoModel:LiveCampaignTriggerInfo){
        fcmService.sendLiveMessage(infoModel)
    }
}