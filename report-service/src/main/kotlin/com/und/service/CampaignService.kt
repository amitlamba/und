package com.und.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.model.jpa.*
import com.und.model.jpa.AbCampaign
import com.und.web.model.AbCampaign as WebAbCampaign
import com.und.repository.jpa.CampaignRepository
import com.und.security.utils.AuthenticationUtils
import com.und.util.loggerFor
import com.und.web.model.Variant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import com.und.web.model.Campaign as WebCampaign
import com.und.model.jpa.Variant as JpaVariant




@Service
class CampaignService {


    companion object {

        protected val logger = loggerFor(CampaignService::class.java)
    }

    @Autowired
    private lateinit var campaignRepository: CampaignRepository

    @Autowired
    private lateinit var objectMapper: ObjectMapper


    fun getCampaignById(campaignId: Long,clientId:Long): WebCampaign {
        //var clientId = AuthenticationUtils.clientID ?: throw AccessDeniedException("")
        var campaign = campaignRepository.findByIdAndClientID(campaignId, clientId)
        if (campaign.isPresent) {
            return buildWebCampaign(campaign = campaign.get())
        } else {
            logger.info("Campaign doesn't exist with id $campaignId and client : $clientId")
            throw RuntimeException("Campaign doesn't exist with id $campaignId and client : $clientId")
        }
    }

    fun buildWebCampaign(campaign: Campaign): WebCampaign {
        val webCampaign = WebCampaign()
        with(webCampaign) {
            id = campaign.id
            name = campaign.name

            campaignType = campaign.campaignType
            segmentationID = campaign.segmentationID
            dateCreated = campaign.dateCreated
            dateModified = campaign.dateModified
            status = campaign.status
            conversionEvent = campaign.conversionEvent
            serviceProviderId = campaign.serviceProviderId
            fromUser = campaign.fromUser
            //Migrate db
            typeOfCampaign = campaign.typeOfCampaign

        }

        if (campaign.startDate != null) {
            val liveSchedule = LiveSchedule()
            liveSchedule.startTime = toCampaignTime(campaign.startDate)
            liveSchedule.endTime = toCampaignTime(campaign.endDate)
            webCampaign.liveSchedule = liveSchedule

        } else webCampaign.schedule = objectMapper.readValue(campaign.schedule, Schedule::class.java)

        if (campaign.emailCampaign != null) {
            val emailcampaign = campaign.emailCampaign
            webCampaign.templateID = emailcampaign?.templateId
            webCampaign.campaignType = CampaignType.EMAIL
        } else if (campaign.smsCampaign != null) {
            val smsCampaign = campaign.smsCampaign
            webCampaign.templateID = smsCampaign?.templateId
            webCampaign.campaignType = CampaignType.SMS
        } else if (campaign.androidCampaign != null) {
            val androidCampaign = campaign.androidCampaign
            webCampaign.campaignType = CampaignType.PUSH_ANDROID
            webCampaign.templateID = androidCampaign?.templateId
        } else if (campaign.webCampaign != null) {
            val webPushCampaign = campaign.webCampaign
            webCampaign.campaignType = CampaignType.PUSH_WEB
            webCampaign.templateID = webPushCampaign?.templateId
        }
//        else if(campaign.iosCampaign!=null){
//            val iosCampaign=campaign.iosCampaign
//            iosCampaign.campaignType=CampaignType.PUSH_IOS
//            iosCampaign.templateID=iosCampaign?.templateId
//        }

        //adding ab campaign
        campaign.abCampaign?.let {
            webCampaign.abCampaign = buildAbCampaign(it)
        }
        //adding variant
        campaign.variants?.let {
            webCampaign.variants = buildVariants(it)
        }


        return webCampaign
    }

    private fun buildAbCampaign(JpaabCampaign: com.und.model.jpa.AbCampaign): WebAbCampaign {
        val abCampaign = WebAbCampaign()
        with(abCampaign) {
            id = JpaabCampaign.id
            runType = JpaabCampaign.runType
            remind = JpaabCampaign.remind
            waitTime = JpaabCampaign.waitTime
            sampleSize = JpaabCampaign.sampleSize
        }
        return abCampaign
    }

    private fun buildVariants(variants: List<JpaVariant>): List<Variant> {
        val webVariants = mutableListOf<Variant>()
        variants.forEach {
            val variant = Variant()

            with(variant) {
                id = it.id
                percentage = it.percentage
                users = it.users
                name = it.name
                winner = it.winner
                templateId = it.templateId

            }
            webVariants.add(variant)
        }
        return webVariants
    }

    fun getListOfCampaign(segmentId: Long,clientId: Long): List<com.und.web.model.Campaign> {
        var campaigns = campaignRepository.findByClientIDAndSegmentationID(clientId, segmentId)
        var listOfCampaign = mutableListOf<com.und.web.model.Campaign>()
        campaigns.forEach {
            var campaign = buildWebCampaign(it)
            listOfCampaign.add(campaign)
        }
        return listOfCampaign
    }
}