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
                emailTemplateId = emailTemplate.id ?: 0L,
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

    fun buildTestCampaignSms(clientId: Long, campaign: WebCampaign, user: EventUser, sms: SmsTemplate): Sms {
        return Sms(
                clientId,
                campaign.fromUser,
                user.identity.mobile,
                smsBody = sms.smsTemplateBody,
                smsTemplateId =  0L,
                smsTemplateName = "",
                eventUser = user,
                serviceProviderId = campaign.serviceProviderId,
                campaignId = -1,
                segmentId = campaign.segmentationID
        )
    }

    fun buildAndroidFcmMessage(clientId: Long, androidCampaign: AndroidCampaign, user: EventUser, campaign: Campaign,templateId:Long?=null): FcmMessage {
        return FcmMessage(
                clientId = clientId,
                templateId = if(templateId==null) androidCampaign.templateId ?: 0L else templateId,
                to = user.identity.androidFcmToken ?: "",
                type = "android",
                campaignId = -1,
                userId = user.id,
                eventUser = user,
                serviceProviderId = campaign.serviceProviderId,
                segmentId = campaign.segmentationID
        )
    }

    fun buildTestCampaignAndroidFcmMessage(clientId: Long, user: EventUser, campaign: Campaign,androidTemplate: AndroidTemplate): FcmMessage {
        return FcmMessage(
                clientId = clientId,
                templateId = 0L,
                to = user.identity.androidFcmToken ?: "",
                type = "android",
                campaignId = -1,
                userId = user.id,
                eventUser = user,
                serviceProviderId = campaign.serviceProviderId,
                segmentId = campaign.segmentationID,
                androidTemplate = androidTemplate
        )
    }

    fun buildWebFcmMessage(clientId: Long, webPushCampaign: WebPushCampaign, token: String, campaign: Campaign, user: EventUser,templateId: Long?=null): FcmMessage {
        return FcmMessage(
                clientId = clientId,
                templateId = if(templateId==null) webPushCampaign.templateId ?: 0L else templateId,
                to = token,
                type = "web",
                campaignId = campaign.id!!,
                userId = user.id,
                eventUser = user,
                serviceProviderId = campaign.serviceProviderId,
                segmentId = campaign.segmentationID
        )
    }

    fun buildTestCampaignWebFcmMessage(clientId: Long, token: String, campaign: Campaign, user: EventUser,webPushTemplate: WebPushTemplate): FcmMessage {
        return FcmMessage(
                clientId = clientId,
                templateId = 0L,
                to = token,
                type = "web",
                campaignId = -1,
                userId = user.id,
                eventUser = user,
                serviceProviderId = campaign.serviceProviderId,
                segmentId = campaign.segmentationID,
                webPushTemplate = webPushTemplate
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