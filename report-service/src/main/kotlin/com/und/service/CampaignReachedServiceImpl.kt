package com.und.service

import com.und.repository.mongo.CampaignReachabilityRepository
import com.und.web.model.CampaignReached
import com.und.web.model.CampaignReachedResult
import com.und.service.CampaignService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CampaignReachedServiceImpl :CampaignReachedService{

    @Autowired
    private lateinit var campaignReachabilityRepository:CampaignReachabilityRepository

    @Autowired
    private lateinit var campaignService:CampaignService

    override fun getCampaignReachability(campaignId: Long,clientId:Long): CampaignReached {
        //var clientId = AuthenticationUtils.clientID ?: throw AccessDeniedException("")
        //here we are finding campaign only to know the type of campaign like android or email
        // if all analytics related info store in one collection then we remove one hit from mongo
        var campaign=campaignService.getCampaignById(campaignId,clientId)
        var result = campaignReachabilityRepository.getCampaignReachability(clientId, campaignId, campaign.campaignType.toString())

        var delivered: Long = getResult("SENT", result)
        var failed: Long = getResult("ERROR", result)
        var read: Long = getResult("READ", result)
        var interacted: Long = getResult("CTA_PERFORMED", result)
        var campaignReached = CampaignReached()
        with(campaignReached) {
            this.delivered = delivered + read +interacted
            this.failed = failed
            this.read = read + interacted
            this.interacted = interacted

        }
        return campaignReached
    }

    private fun getResult(type:String,result:List<CampaignReachedResult>):Long{
       return result.find { it.id.equals(type) }?.count?:0
    }
}