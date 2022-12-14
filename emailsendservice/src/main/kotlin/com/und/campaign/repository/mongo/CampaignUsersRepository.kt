package com.und.campaign.repository.mongo

import com.und.campaign.model.CampaignUsers
import org.springframework.data.mongodb.repository.MongoRepository

interface CampaignUsersRepository:MongoRepository<CampaignUsers,String> {
    fun findByClientIdAndCampaignIdAndExecutionId(clientId:Long,campaignId:Long,executionId:String):List<CampaignUsers>
}