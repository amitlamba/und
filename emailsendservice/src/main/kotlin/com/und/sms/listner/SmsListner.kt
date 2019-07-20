package com.und.sms.listner

import com.und.campaign.model.CampaignUserStatus
import com.und.campaign.model.CampaignUsers
import com.und.campaign.repository.jpa.CampaignRepository
import com.und.campaign.repository.jpa.EmailCampaignRepository
import com.und.campaign.repository.jpa.SmsCampaignRepository
import com.und.campaign.repository.mongo.CampaignUsersRepository
import com.und.campaign.repository.mongo.EventUserRepository
import com.und.common.utils.BuildCampaignMessage
import com.und.email.repository.jpa.EmailTemplateRepository
import com.und.exception.EmailFailureException
import com.und.model.utils.LiveCampaignTriggerInfo
import com.und.model.utils.Sms
import com.und.repository.jpa.CampaignTriggerInfoRepository
import com.und.service.CommonSmsService
import com.und.sms.repository.jpa.SmsTemplateRepository
import com.und.sms.service.SmsService
import com.und.utils.loggerFor
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class SmsListner {
    companion object {
        val logger = loggerFor(SmsListner::class.java)
    }

    @Autowired
    private lateinit var smsService: SmsService

    @Autowired
    private lateinit var buildCampaignMessage: BuildCampaignMessage

    @Autowired
    private lateinit var eventUserRepository: EventUserRepository
    @Autowired
    private lateinit var campaignRepository: CampaignRepository
    @Autowired
    private lateinit var smsCampaignRepository: SmsCampaignRepository
    @Autowired
    private lateinit var smsTemplateRepository: SmsTemplateRepository
    @Autowired
    private lateinit var campaignUsersRepository: CampaignUsersRepository
    @Autowired
    private lateinit var campaignTriggerInfoRepository: CampaignTriggerInfoRepository

    @Autowired
    @Qualifier("testsmsservice")
    lateinit var commonSmsService: CommonSmsService

    @StreamListener("smsEventReceive")
    fun sendSmsCampaign(campaignUsers: CampaignUsers) {
        //smsService.sendSms(sms)
        //check error in campaign triggerinfo
        val campaignTriggerInfo = campaignTriggerInfoRepository.findById(campaignUsers.campaignId)
        if(campaignTriggerInfo.isPresent && !campaignTriggerInfo.get().error) {
            val clientId = campaignUsers.clientId
            val campaignId = campaignUsers.campaignId
            val templateId = campaignUsers.templateId
            val campaign = campaignRepository.getCampaignByCampaignId(campaignId, clientId).get()
            val smsTemplate = smsTemplateRepository.findByIdAndClientID(templateId, clientId)!!
            val smsCampaign = smsCampaignRepository.findByCampaignId(campaignId).get()
            when (campaignUsers.groupStatus) {
                GroupStatus.ERROR -> {
                    campaignUsers.users.forEachIndexed { index, value ->
                        val status = value["status"]
                        val userId = value["userId"] as String
                        val eventUser = eventUserRepository.findByIdAndClientId(ObjectId(userId), clientId)
                        eventUser?.let {
                            val sms = buildCampaignMessage.buildSms(clientId, campaign, eventUser, smsCampaign, smsTemplate)
                            when (status) {
                                CampaignUserStatus.UNDELIVERED -> {
                                    try {
                                        smsService.sendSms(sms)
                                        updateCampaignUserDocument(null, campaignUsers)
                                    } catch (ex: Exception) {
                                        campaignTriggerInfoRepository.updateErrorStatus(campaignId,true)
                                        updateCampaignUserDocument(index, campaignUsers)
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
                            val sms = buildCampaignMessage.buildSms(clientId = clientId, campaign = campaign, user = eventUser, smsCampaign = smsCampaign, smsTemplate = smsTemplate)
                            try {
                                smsService.sendSms(sms)
                                updateCampaignUserDocument(null, campaignUsers)
                            } catch (ex: Exception) {
                                campaignTriggerInfoRepository.updateErrorStatus(campaignId,true)
                                updateCampaignUserDocument(index, campaignUsers)
                                // emailService.toKafkaEmailError(ex.error)
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

    @StreamListener("inTestSms")
    fun sendTestSms(sms:Sms){
        commonSmsService.sendSms(sms)
    }

    @StreamListener("inSmsLiveCampaign")
    fun inSmsLiveCampaign(infoModel:LiveCampaignTriggerInfo){
        smsService.sendLiveSms(infoModel)
    }

}

enum class GroupStatus{
    DELIVERED,
    ERROR,
    UNDELIVERED
}