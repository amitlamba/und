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
import com.und.fcmpush.service.FcmSendService
import com.und.model.utils.FcmMessage
import com.und.fcmpush.service.FcmService
import com.und.fcmpush.service.NotificationError
import com.und.model.jpa.Campaign
import com.und.model.mongo.EventUser
import com.und.model.utils.CampaignType
import com.und.model.utils.LiveCampaignTriggerInfo
import com.und.repository.jpa.CampaignTriggerInfoRepository
import com.und.sms.listner.GroupStatus
import com.und.utils.loggerFor
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.support.MessageBuilder
import org.springframework.orm.jpa.JpaSystemException
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class FcmListner {

    @Autowired
    private lateinit var fcmSendService: FcmSendService

    @Autowired
    @Qualifier("testfcmservice")
    lateinit var fcmTestService : FcmService

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

    companion object {
        val logger = loggerFor(FcmListner::class.java)
    }


    //TODO make reties 0
    @StreamListener("fcmEventReceive")
    fun sendMessage(campaignUsers: CampaignUsers) {
        val campaignTriggerInfo = campaignTriggerInfoRepository.findById(campaignUsers.campaignId)
        logger.debug("Sending fcm message for clientId ${campaignUsers.clientId} campaignId ${campaignUsers.campaignId} groupId " +
                "${campaignUsers.groupId} executionId ${campaignUsers.executionId}")
        if(campaignTriggerInfo.isPresent && !campaignTriggerInfo.get().error) {
            val clientId = campaignUsers.clientId
            val campaignId = campaignUsers.campaignId
            val templateId = campaignUsers.templateId
            val campaign = campaignRepository.findById(campaignId).get()
            when (campaignUsers.groupStatus) {
                GroupStatus.ERROR -> {
                    campaignUsers.users.forEachIndexed { index, value ->
                        val status = value["status"] as String
                        val userId = value["userId"] as String
                        val eventUser = eventUserRepository.findByIdAndClientId(ObjectId(userId), clientId)
                        eventUser?.let {
                            when (CampaignUserStatus.valueOf(status)) {
                                CampaignUserStatus.UNDELIVERED -> {
                                    try {
                                        sendCampaignMessage(campaign, templateId, it)
                                    } catch (ex: FcmFailureException) {
                                        //TODO pause campaign make a feign call
                                        try {
                                            campaignTriggerInfoRepository.updateErrorStatus(campaignId, true)
                                        }catch(ex: JpaSystemException){

                                        }
                                        updateCampaignUserDocument(index, campaignUsers)
                                        logger.info("Error occurred during sending fcm message for clientId ${campaignUsers.clientId}" +
                                                "campaignId ${campaignUsers.campaignId} groupId ${campaignUsers.groupId} error is ${ex.error.message}")
                                        toFcmFailureKafka(ex.error)
                                        return
                                    }
                                }
                            }
                        }
                    }
                    updateCampaignUserDocument(null, campaignUsers)
                }
                GroupStatus.UNDELIVERED -> {
                    campaignUsers.users.forEachIndexed { index, value ->
                        val userId = value["userId"] as String
                        val eventUser = eventUserRepository.findByIdAndClientId(ObjectId(userId), clientId)
                        eventUser?.let {
                            try {
                                sendCampaignMessage(campaign, templateId, it)
                                //updateCampaignUserDocument(null, campaignUsers)
                            } catch (ex: FcmFailureException) {
                                //TODO pause campaign make a feign call
                                try {
                                    campaignTriggerInfoRepository.updateErrorStatus(campaignId, true)
                                }catch(ex:JpaSystemException){

                                }
                                updateCampaignUserDocument(index, campaignUsers)
                                toFcmFailureKafka(ex.error)
                                logger.info("Error occured during sending fcm message for clientId ${campaignUsers.clientId}" +
                                        "campaignId ${campaignUsers.campaignId} groupId ${campaignUsers.groupId} error is ${ex.error.message}")
                                return
                            }
                        }
                    }
                    updateCampaignUserDocument(null, campaignUsers)
                }
            }
        }else{
            logger.debug("Skipped (due to error) sending fcm message for clientId ${campaignUsers.clientId} campaignId ${campaignUsers.campaignId} groupId " +
                    "${campaignUsers.groupId} executionId ${campaignUsers.executionId}")
        }
    }
    fun updateCampaignUserDocument(errorPosition:Int?,campaignUsers: CampaignUsers){
        errorPosition?.let {
            val users = campaignUsers.users.mapIndexed { index, document ->
                if(index>=errorPosition){
                    var document1 = Document()
                    document1.put("userId",document["userId"])
                    document1.put("status",CampaignUserStatus.UNDELIVERED.name)
                    document1
//                    Document(mapOf(Pair("userId",document["userId"]),Pair("status", CampaignUserStatus.UNDELIVERED)))
                }else{ var document1 = Document()
                    document1.put("userId",document["userId"])
                    document1.put("status",CampaignUserStatus.DELIVERED.name)
                    document1
//                    Document(mapOf(Pair("userId",document["userId"]),Pair("status", CampaignUserStatus.DELIVERED)))
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
        logger.info("sending fcm test campaign for client ${message.clientId}.")
        fcmTestService.sendMessage(message)
    }

    @StreamListener("inFcmLiveCampaign")
    fun inFcmLiveCampaign(infoModel:LiveCampaignTriggerInfo){
        logger.debug("sending fcm message for live campaign clientId ${infoModel.clientId} campaignId ${infoModel.campaignId} " +
                "templateId ${infoModel.templateId}")
        fcmSendService.sendLiveMessage(infoModel)
    }
}