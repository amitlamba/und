package com.und.common.utils

import com.und.model.jpa.*
import com.und.model.mongo.EventUser
import com.und.model.utils.Email
import com.und.model.utils.Campaign as WebCampaign
import com.und.model.utils.EmailTemplate as WebEmailTemplate
import com.und.model.utils.FcmMessage
import com.und.model.utils.Sms
import org.springframework.stereotype.Component
import javax.mail.internet.InternetAddress

@Component
class BuildCampaignMessage {
    fun buildSms(clientId: Long, campaign: Campaign, user: EventUser, smsCampaign: SmsCampaign, smsTemplate: EmailTemplate): Sms {
        return Sms(
                clientId,
                campaign.fromUser,
                user.identity.mobile,
                smsBody = null,
                smsTemplateId = smsCampaign.templateId ?: 0L,
                smsTemplateName = smsTemplate.name,
                eventUser = user,
                serviceProviderId = campaign.serviceProviderId,
                campaignId = campaign.id,
                segmentId = campaign.segmentationID
        )
    }

    fun buildEmail(clientId: Long, campaign: Campaign, user: EventUser, emailCampaign: EmailCampaign, emailTemplate: EmailTemplate): Email {
        return Email(
                clientID = clientId,
                fromEmailAddress = InternetAddress.parse(campaign.fromUser, false)[0],
                toEmailAddresses = InternetAddress.parse(user.identity.email, false),
                emailTemplateId = emailCampaign.templateId ?: 0L,
                emailTemplateName = emailTemplate.name,
                campaignId = campaign.id!!,
                eventUser = user,
                clientEmailSettingId = emailCampaign.clientSettingEmailId,
                segmentId = campaign.segmentationID
        )
    }

    fun buildTestCampaignEmail(clientId: Long, campaign: WebCampaign, user: EventUser, emailTemplate: WebEmailTemplate): Email {
        return Email(
                clientID = clientId,
                fromEmailAddress = InternetAddress.parse(campaign.fromUser, false)[0],
                toEmailAddresses = InternetAddress.parse(user.identity.email, false),
                emailTemplateId = 0L,
                emailTemplateName = "",
                campaignId = -1,
                emailBody = emailTemplate.emailTemplateBody,
                emailSubject = emailTemplate.emailTemplateSubject,
                eventUser = user,
                clientEmailSettingId = campaign.clientEmailSettingId,
                segmentId = -1
        )
    }

    fun buildAndroidFcmMessage(clientId: Long, androidCampaign: AndroidCampaign, user: EventUser, campaign: Campaign): FcmMessage {
        return FcmMessage(
                clientId = clientId,
                templateId = androidCampaign.templateId ?: 0L,
                to = user.identity.androidFcmToken ?: "",
                type = "android",
                campaignId = campaign.id!!,
                userId = user.id,
                eventUser = user,
                serviceProviderId = campaign.serviceProviderId,
                segmentId = campaign.segmentationID
        )
    }
    fun buildWebFcmMessage(clientId: Long, webPushCampaign: WebPushCampaign, token: String, campaign: Campaign, user: EventUser): FcmMessage {
        return FcmMessage(
                clientId = clientId,
                templateId = webPushCampaign.templateId ?: 0L,
                to = token,
                type = "web",
                campaignId = campaign.id!!,
                userId = user.id,
                eventUser = user,
                serviceProviderId = campaign.serviceProviderId,
                segmentId = campaign.segmentationID
        )
    }
    fun buildIosFcmMessage(clientId: Long, user: EventUser, campaign: Campaign): FcmMessage {
        return FcmMessage(
                clientId = clientId,
                //                templateId = iosCampaign.get().templateId?:0L,
                templateId = 0L,
                to = user.identity.iosFcmToken ?: "",
                type = "ios",
                campaignId = campaign.id!!,
                userId = user.id,
                eventUser = user,
                serviceProviderId = campaign.serviceProviderId,
                segmentId = campaign.segmentationID
        )
    }
}