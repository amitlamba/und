package com.und.service

import com.und.config.EventStream
import com.und.model.jpa.Campaign
import com.und.model.mongo.EventUser
import com.und.model.utils.Email
import com.und.model.utils.Sms
import com.und.repository.jpa.CampaignRepository
import com.und.utils.loggerFor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import javax.mail.internet.InternetAddress

@Service
class CampaignService {

    companion object {
        protected val logger = loggerFor(CampaignService::class.java)
    }

    @Autowired
    private lateinit var campaignRepository: CampaignRepository

    @Autowired
    private lateinit var segmentService: SegmentService

    @Autowired
    private lateinit var eventStream: EventStream

    fun executeCampaign(campaignId: Long, clientId: Long) {
        val campaignOption  = campaignRepository.getCampaignByCampaignId(campaignId, clientId)
        val campaign =  campaignOption.orElseThrow( {IllegalStateException("campaign not found for campaign id $campaignId and client $clientId")})
        val usersData = getUsersData(campaign.segmentId , clientId)
        usersData.forEach { user ->
            try {
                //TODO: filter out unsubscribed and blacklisted users
                //TODO: How to skip transactional Messages

                //check mode of communication is email
                if(campaign?.campaignType=="EMAIL"){

                    if(user.communication?.email?.dnd == true)
                        return@forEach //Local lambda return
                    val email: Email = email(clientId, campaign, user)
                    toKafka(email)
                }
                //check mode of communication is sms
                if(campaign?.campaignType=="SMS"){

                    if(user.communication?.mobile?.dnd == true)
                        return@forEach //Local lambda return
                    val sms: Sms = sms(clientId, campaign, user)
                    toKafka(sms)
                }
            } catch (ex: Exception) {
                logger.error(ex.message)

            } finally {

            }
        }
    }


    private fun sms(clientId: Long, campaign: Campaign?, user: EventUser):Sms{
        return Sms(
                clientId,
                campaign?.fromSMSUser,
                user.identity.mobile,
                smsBody =null,
                smsTemplateId = campaign?.smsTemplateId?:0L,
                //assign name also
                smsTemplateName=null,
                eventUser = user
        )
    }

    private fun email(clientId: Long, campaign: Campaign, user: EventUser): Email {
        return Email(
                clientID = clientId,
                fromEmailAddress = InternetAddress.parse(campaign.fromEmailAddress, false)[0],
                toEmailAddresses = InternetAddress.parse(user.identity.email, false),
                emailTemplateId = campaign.emailTemplateId ?: 0L,
                emailTemplateName = campaign.emailTemplateName ?: "",
                campaignId = campaign.campaignId
        )
    }

    fun getUsersData(segmentId: Long, clientId: Long): List<EventUser> {
        val segment = segmentService.getWebSegment(segmentId, clientId)
        return segmentService.getUserData(segment, clientId)
    }

    fun toKafka(email: Email): Boolean =
            eventStream.emailEventSend().send(MessageBuilder.withPayload(email).build())


    fun toKafka(sms: Sms): Boolean =
            eventStream.smsEventSend().send(MessageBuilder.withPayload(sms).build())


}