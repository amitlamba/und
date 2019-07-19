package com.und.campaign.repository.redis

import com.und.model.redis.LiveSegmentCampaignCache
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface LiveSegmentCampaignRepository:CrudRepository<LiveSegmentCampaignCache,String> {
}