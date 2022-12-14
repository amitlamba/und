package com.und.campaign.service

import com.und.campaign.repository.mongo.EventUserRepository
import com.und.common.utils.BuildCampaignMessage
import com.und.config.EventStream
import com.und.model.jpa.AndroidTemplate
import com.und.model.jpa.SmsTemplate
import com.und.model.jpa.WebPushTemplate
import com.und.model.mongo.EventUser
import com.und.model.utils.*
import com.und.service.SegmentService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import java.lang.Exception

@Service
class TestCampaignService {

    companion object {
        val logger=LoggerFactory.getLogger(TestCampaignService::class.java)
    }
    @Autowired
    lateinit var segmentService: SegmentService
    @Autowired
    lateinit var buildCampaignMessage:BuildCampaignMessage

    @Autowired
    lateinit var eventUserRepository: EventUserRepository

    @Autowired
    private lateinit var eventStream: EventStream

    fun executeTestCampaign(testCampaign:TestCampaign){
        logger.info("Test campaign is received for client ${testCampaign.clientId}")
        val campaign=testCampaign.campaign
        val clientId= testCampaign.clientId!!
        var userList :List<EventUser> ?=null

        campaign.segmentationID?.let {
            userList = segmentService.getUserData(it,clientId,testCampaign.type.name)
        }

        if(userList == null){
            //Here we need to find event user by email,token etc.
            userList = when(testCampaign.findByType){
                "Email" -> findByEmail(clientId,testCampaign.toAddresses !!)
                "UserNDot ID" -> findByUserNDotId(clientId,testCampaign.toAddresses!!)
                "Mobile Number" -> findByMobile(clientId,testCampaign.toAddresses!!)
                "Client User ID" -> findByClientUserId(clientId,testCampaign.toAddresses!!)
                "TOKEN" -> when(testCampaign.type){
                    CampaignType.PUSH_WEB -> findByWebToken(clientId,testCampaign.toAddresses!!)
                    CampaignType.PUSH_IOS -> emptyList()
                    CampaignType.PUSH_ANDROID -> findByAndroidToken(clientId,testCampaign.toAddresses!!)
                    else -> emptyList()
                }
                else -> emptyList()
            }
        }
        logger.info("No of users to send test mail are ${userList?.size}")
        execute(testCampaign, userList, campaign)

    }

    private fun execute(testCampaign: TestCampaign, userList: List<EventUser>?, campaign: Campaign) {
        try {
            when (testCampaign.type) {
                CampaignType.EMAIL -> {

                    userList?.forEach {
                        executeEmailCampaignForUser(campaign = campaign,
                                user = it,
                                clientId = it.clientId.toLong(),
                                emailTemplate = testCampaign.emailTemplate!!)

                    }
                }
                CampaignType.SMS -> {
                    userList?.forEach {
                        executeSmsCampaignForUser(campaign=campaign,
                                user = it,
                                clientId = it.clientId.toLong(),
                                smsTemplate = testCampaign.smsTemplate!!)
                    }
                }
                CampaignType.PUSH_ANDROID -> {
                    userList?.forEach {
                        executeAndroidCampaignForUser(campaign = campaign,
                                user = it,
                                clientId = it.clientId.toLong(),
                                androidTemplate = testCampaign.androidTemplate!!)
                    }
                }
                CampaignType.PUSH_IOS -> {
                    userList?.forEach {

                    }
                }
                CampaignType.PUSH_WEB -> {
                    userList?.forEach {
                        executeWebCampaignForUser(campaign = campaign,
                                user = it,
                                clientId = it.clientId.toLong(),
                                webPushTemplate = testCampaign.webTemplate!!)
                    }
                }
            }
        } catch (ex: Exception) {
            logger.error(ex.message)
        }
    }

    private fun executeEmailCampaignForUser(campaign: Campaign, user: EventUser, clientId: Long,emailTemplate: EmailTemplate){
        if (user.communication?.email==null || user.communication?.email?.dnd == true)
            return //Local lambda return
        val email=buildCampaignMessage.buildTestCampaignEmail(clientId,campaign,user,emailTemplate)
        eventStream.testEmailCampaign().send(MessageBuilder.withPayload(email).build())
        //commonEmailService.sendEmail(email)
    }

    private fun executeSmsCampaignForUser(campaign: Campaign, user: EventUser, clientId: Long,smsTemplate: SmsTemplate){
        if (user.communication?.mobile==null || user.communication?.mobile?.dnd == true)
            return //Local lambda return
        val sms=buildCampaignMessage.buildTestCampaignSms(clientId,campaign,user,smsTemplate)
        eventStream.testSmsCampaign().send(MessageBuilder.withPayload(sms).build())
        //commonSmsService.sendSms(sms)
    }

    private fun executeAndroidCampaignForUser(campaign: Campaign, user: EventUser, clientId: Long,androidTemplate: AndroidTemplate){
        if (user.communication?.android==null || user.communication?.android?.dnd == true)
            return //Local lambda return
        val jpaCampaign=buildCampaign(campaign,clientId)
        val message=buildCampaignMessage.buildTestCampaignAndroidFcmMessage(clientId,user,jpaCampaign,androidTemplate)
        eventStream.testFcmCampaign().send(MessageBuilder.withPayload(message).build())
        //fcmService.sendMessage(message)
    }

    private fun executeWebCampaignForUser(campaign: Campaign, user: EventUser, clientId: Long,webPushTemplate: WebPushTemplate){
        if (user.communication?.webpush ==null || user.communication?.webpush?.dnd == true)
            return //Local lambda return
        val jpaCampaign=buildCampaign(campaign,clientId)
        user.identity.webFcmToken?.forEach {
            val message=buildCampaignMessage.buildTestCampaignWebFcmMessage(clientId,it,jpaCampaign,user,webPushTemplate)
            eventStream.testFcmCampaign().send(MessageBuilder.withPayload(message).build())
            //fcmService.sendMessage(message)
        }
    }

    private fun findByEmail(clientId: Long,id: Array<String>):List<EventUser>{
        return eventUserRepository.findByEmailIn(clientId,id)
    }

    private fun findByUserNDotId(clientId: Long,id:Array<String>):List<EventUser>{
        return eventUserRepository.findByUndIdIn(clientId,id)
    }


    private fun findByClientUserId(clientId: Long,id: Array<String>):List<EventUser>{
        return eventUserRepository.findByUidIn(clientId,id)
    }


    private fun findByMobile(clientId: Long,id: Array<String>):List<EventUser>{
        return eventUserRepository.findByMobileIn(clientId,id)
    }

    private fun findByAndroidToken(clientId: Long,id: Array<String>):List<EventUser>{
        return eventUserRepository.findByAndroidFcmTokenIn(clientId,id)
    }

    private fun findByWebToken(clientId: Long,id: Array<String>):List<EventUser>{
        return eventUserRepository.findByWebFcmTokenIn(clientId,id)
    }

    fun buildCampaign(webCampaign: Campaign,clientId: Long): com.und.model.jpa.Campaign {
        val campaign = com.und.model.jpa.Campaign()

        with(campaign) {
            segmentationID = webCampaign.segmentationID
            serviceProviderId = webCampaign.serviceProviderId
        }
        return campaign
    }
}