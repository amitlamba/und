package com.und.kafkalisterner

import com.und.service.CampaignService
import com.und.utils.loggerFor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.stereotype.Service

@Service
class CampaignListener {

    @Autowired
    private lateinit var campaignService: CampaignService

    companion object {
        val logger = loggerFor(CampaignListener::class.java)
    }

/*   @StreamListener("campaignTriggerReceive")
    fun executeCampaign(campaignMap: Map<String,Long>) {
        campaignService.executeCampaign(campaignMap["campaignMap"]!!, campaignMap["clientId"]!!)
    }*/

   @StreamListener("campaignTriggerReceive")
    fun executeCampaign(campaignData: Pair<Long, Long>) {
       try {
           val (campaignId, clientId) = campaignData
           logger.debug("campaign trigger with id $campaignId and $clientId")
           campaignService.executeCampaign(campaignId, clientId)
       }catch (ex:Exception) {
         logger.error("error occurred", ex)
       }finally {
           logger.info("complete")
       }
    }



}