package com.und.service

import com.und.common.utils.BuildCampaignMessage
import com.und.model.mongo.EventUser
import com.und.model.utils.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
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
    @Qualifier("testemailservice")
    lateinit var commonEmailService: CommonEmailService

    fun executeTestCampaign(testCampaign:TestCampaign){

        val campaign=testCampaign.campaign
        val clientId= testCampaign.clientId!!
        var userList :List<EventUser> ?=null

        campaign.segmentationID?.let {
            userList = segmentService.getUserData(it,clientId)
        }

        if(userList == null){
            //Here we need to find event user by email,token etc.
            userList = when(testCampaign.findByType){
                "Email" -> findByEmail(clientId,testCampaign.toAddresses !!)
                "UserNDot ID" -> findByUserNDotId(clientId,testCampaign.toAddresses!!)
                "Mobile Number" -> findByMobile(clientId,testCampaign.toAddresses!!)
                "Client User ID" -> findByClientUserId(clientId,testCampaign.toAddresses!!)
                "TOKEN" -> findByToken(clientId,testCampaign.toAddresses!!)
                else -> emptyList()
            }
        }

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

                    }
                }
                CampaignType.PUSH_ANDROID -> {
                    userList?.forEach {

                    }
                }
                CampaignType.PUSH_IOS -> {
                    userList?.forEach {

                    }
                }
                CampaignType.PUSH_WEB -> {
                    userList?.forEach {

                    }
                }
            }
        } catch (ex: Exception) {
            logger.error(ex.message)
        }
    }

    private fun executeEmailCampaignForUser(campaign: Campaign, user: EventUser, clientId: Long,emailTemplate: EmailTemplate){
        if (user.communication?.email?.dnd == true)
            return //Local lambda return
        val email=buildCampaignMessage.buildTestCampaignEmail(clientId,campaign,user,emailTemplate)
        commonEmailService.sendEmail(email)
    }

    private fun findByEmail(clientId: Long,id: Array<String>):List<EventUser>{
        return emptyList()
    }

    private fun findByUserNDotId(clientId: Long,id:Array<String>):List<EventUser>{
        return emptyList()
    }


    private fun findByClientUserId(clientId: Long,id: Array<String>):List<EventUser>{
        return emptyList()
    }


    private fun findByMobile(clientId: Long,id: Array<String>):List<EventUser>{
        return emptyList()
    }

    private fun findByToken(clientId: Long,id: Array<String>):List<EventUser>{
        return emptyList()
    }
}