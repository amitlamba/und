package com.und.model.redis

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import java.time.LocalDateTime


class LiveSegmentCampaign {
    var campaignId:Long=0
    var status:String=""
    var startDate:LocalDateTime?=null
}

@RedisHash("liveSegmentCampaigns")
class LiveSegmentCampaignCache{
    @Id
    lateinit var id:String
    var liveSegmentCampaign:List<LiveSegmentCampaign> = emptyList()
}