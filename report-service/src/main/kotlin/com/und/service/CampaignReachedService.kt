package com.und.service

import com.und.web.model.CampaignReached
import org.springframework.stereotype.Service

@Service
interface CampaignReachedService {

    fun getCampaignReachability(campaignId:Long,clientId:Long):CampaignReached
}