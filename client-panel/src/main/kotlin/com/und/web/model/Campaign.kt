package com.und.web.model

import com.und.model.CampaignStatus
import com.und.model.jpa.CampaignType
import com.und.model.jpa.Schedule
import java.time.LocalDateTime


class Campaign {
    var id: Long? = null
    var name: String = ""
    var schedule: Schedule? = null
    var campaignType: CampaignType? = null
    var segmentationID: Long? = null
    var templateID: Long? = null
    var status: CampaignStatus? = null
    var dateCreated: LocalDateTime? = null
    var dateModified: LocalDateTime? = null

}

