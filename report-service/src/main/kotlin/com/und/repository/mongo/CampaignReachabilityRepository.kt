package com.und.report.repository.mongo

import com.und.report.web.model.CampaignReachedResult
import org.springframework.stereotype.Repository

@Repository
interface CampaignReachabilityRepository{

    fun getCampaignReachability(clientId:Long,campaignId:Long,campaignType:String):List<CampaignReachedResult>
}