package com.und.model.utils

import java.time.LocalDateTime
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

class Campaign {
    var id: Long? = null
    var name: String = ""
//    var schedule: Schedule? = null
    lateinit var campaignType: CampaignType
    @NotNull
    var segmentationID: Long?=null
    var templateID: Long?=null
//    var status: CampaignStatus? = null
    var dateCreated: LocalDateTime? = null
    var dateModified: LocalDateTime? = null
    var conversionEvent:String?=null
    var serviceProviderId:Long?=null
    var fromUser:String?=null
    var clientEmailSettingId:Long?=null
//    var liveSchedule: LiveSchedule? = null

}