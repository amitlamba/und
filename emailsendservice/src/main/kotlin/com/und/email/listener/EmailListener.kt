package com.und.email.listener

import com.netflix.discovery.converters.Auto
import com.und.campaign.model.CampaignUserStatus
import com.und.campaign.model.CampaignUsers
import com.und.campaign.repository.jpa.CampaignRepository
import com.und.campaign.repository.jpa.EmailCampaignRepository
import com.und.campaign.repository.mongo.CampaignUsersRepository
import com.und.campaign.repository.mongo.EventUserRepository
import com.und.common.utils.BuildCampaignMessage
import com.und.email.repository.jpa.EmailTemplateRepository
import com.und.exception.EmailFailureException
import com.und.model.utils.Email
import com.und.model.utils.EmailUpdate
import com.und.email.service.EmailHelperService
import com.und.email.service.EmailService
import com.und.model.utils.LiveCampaignTriggerInfo
import com.und.repository.jpa.CampaignTriggerInfoRepository
import com.und.service.CommonEmailService
import com.und.sms.listner.GroupStatus
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
class EmailListener {


    @Autowired
    private lateinit var emailService: EmailService

    @Autowired
    private lateinit var emailHelperService: EmailHelperService

    @Autowired
    private lateinit var buildCampaignMessage: BuildCampaignMessage

    @Autowired
    private lateinit var eventUserRepository:EventUserRepository
    @Autowired
    private lateinit var campaignRepository:CampaignRepository
    @Autowired
    private lateinit var emailCampaignRepository:EmailCampaignRepository
    @Autowired
    private lateinit var emailTemplateRepository:EmailTemplateRepository
    @Autowired
    private lateinit var campaignUsersRepository: CampaignUsersRepository

    @Autowired
    @Qualifier("testemailservice")
    lateinit var commonEmailService: CommonEmailService

    @Autowired
    private lateinit var campaignTriggerInfoRepository: CampaignTriggerInfoRepository

    companion object {
        val logger = loggerFor(EmailListener::class.java)
    }

    @StreamListener("emailEventReceive")
    fun sendEmailCampaign(campaignUsers: CampaignUsers) {
        val campaignTriggerInfo = campaignTriggerInfoRepository.findById(campaignUsers.campaignId)
        logger.debug("Sending email for clientId ${campaignUsers.clientId} campaignId ${campaignUsers.campaignId} groupId " +
                "${campaignUsers.groupId} executionId ${campaignUsers.executionId}")
        if(campaignTriggerInfo.isPresent && !campaignTriggerInfo.get().error) {
            val clientId = campaignUsers.clientId
            val campaignId = campaignUsers.campaignId
            val templateId = campaignUsers.templateId
            val campaign = campaignRepository.findById(campaignId).get()
            val emailTemplate = emailTemplateRepository.findByIdAndClientID(templateId, clientId).get()
            val emailCampaign = emailCampaignRepository.findByCampaignId(campaignId).get()
            when (campaignUsers.groupStatus) {
                GroupStatus.ERROR -> {
                    campaignUsers.users.forEachIndexed { index, value ->
                        val status = value["status"]
                        val userId = value["userId"] as String
                        val eventUser = eventUserRepository.findByIdAndClientId(ObjectId(userId), clientId)
                        eventUser?.let {
                            val email = buildCampaignMessage.buildEmail(clientId, campaign, eventUser, emailCampaign, emailTemplate)
                            when (status) {
                                CampaignUserStatus.UNDELIVERED -> {
                                    try {
                                        sendEmail(email)
                                    } catch (ex: EmailFailureException) {
                                        campaignTriggerInfoRepository.updateErrorStatus(campaignId, true)
                                        updateCampaignUserDocument(index, campaignUsers)
                                        //TODO pause campaign make a feign call
                                        emailService.toKafkaEmailError(ex.error)
                                        logger.info("Error occurred during sending email.groupId ${campaignUsers.groupId} error is  ${ex.error.causeMessage}")
                                        return@forEachIndexed
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
                            val email = buildCampaignMessage.buildEmail(clientId = clientId, campaign = campaign, user = eventUser, emailCampaign = emailCampaign, emailTemplate = emailTemplate)
                            try {
                                sendEmail(email)
                            } catch (ex: EmailFailureException) {
                                campaignTriggerInfoRepository.updateErrorStatus(campaignId, true)
                                updateCampaignUserDocument(index, campaignUsers)
                                //TODO pause campaign make a feign call
                                emailService.toKafkaEmailError(ex.error)
                                logger.info("Error occurred during sending email. groupId ${campaignUsers.groupId} error is ${ex.error.causeMessage}")
                                return@forEachIndexed
                            }
                        }
                    }
                    updateCampaignUserDocument(null, campaignUsers)
                }
                else -> {

                }
            }
        }else{
            logger.debug("Skipped (due to error) sending email for clientId ${campaignUsers.clientId} campaignId ${campaignUsers.campaignId} groupId " +
                    "${campaignUsers.groupId} executionId ${campaignUsers.executionId}")
        }
    }
    fun updateCampaignUserDocument(errorPosition:Int?,campaignUsers: CampaignUsers){
        errorPosition?.let {
            val users = campaignUsers.users.mapIndexed { index, document ->
                if(index>=errorPosition){
                    Document(mapOf(Pair("userId",document["userId"]),Pair("status",CampaignUserStatus.UNDELIVERED)))
                }else{
                    Document(mapOf(Pair("userId",document["userId"]),Pair("status",CampaignUserStatus.DELIVERED)))
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
    @StreamListener("clientEmailReceive")
    fun sendClientEmail(email: Email) {
//        email.clientID = 1
        email.tmpltVisiability=true;
        try {
            sendEmail(email)
        }catch (ex:EmailFailureException){
            emailService.toKafkaEmailError(ex.error)
        }
    }

    private fun sendEmail(email: Email)  {
        var retry = false
        do {
            try {
                emailService.sendEmail(email)
            } catch (ef: EmailFailureException) {
                if (ef.error.retry && ef.error.retries <= 3) {
                    retry = true
                } else {
                    throw ef
                }
            }
        } while (retry)
    }

    @StreamListener("EmailUpdateReceive")
    fun listenEmailUpdate(emailUpdate: EmailUpdate) {
        try {
            emailHelperService.updateEmailStatus(
                    emailUpdate.mongoEmailId,
                    emailUpdate.emailStatus,
                    emailUpdate.clientID,
                    emailUpdate.eventId)
        } catch (ex: Exception) {
            logger.error("Error while Updating Email $emailUpdate", ex.message)
        }
    }

    @StreamListener(value = "VerificationEmailReceive")
    fun sendVerificationEmail(email: Email) {
        emailService.sendVerificationEmail(email)
    }

    @StreamListener("inTestEmail")
    fun sendTestEmail(email:Email){
        logger.info("sending test email.")
        commonEmailService.sendEmail(email)
    }

    @StreamListener("inEmailLiveCampaign")
    fun inEmailLiveCampaign(infoModel:LiveCampaignTriggerInfo){
        logger.debug("sending email for live campaign clientId ${infoModel.clientId} campaignId ${infoModel.campaignId} " +
                "templateId ${infoModel.templateId}")
        emailService.sendLiveEmail(infoModel)
    }
}
