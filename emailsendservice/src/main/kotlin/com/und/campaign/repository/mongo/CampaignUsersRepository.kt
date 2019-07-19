package com.und.campaign.repository.mongo

import com.und.campaign.model.CampaignUsers
import org.springframework.data.mongodb.repository.MongoRepository

interface CampaignUsersRepository:MongoRepository<CampaignUsers,String> {

}