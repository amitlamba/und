package com.und.model.redis

import java.time.LocalDateTime

class LiveSegmentCampaign {
    var campaignId:Long=0
    var startDate: LocalDateTime = LocalDateTime.now()
    var endDate: LocalDateTime = LocalDateTime.now()
    var status:String=""
}