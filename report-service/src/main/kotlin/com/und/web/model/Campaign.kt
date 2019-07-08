package com.und.web.model

import com.und.model.CampaignStatus
import com.und.model.jpa.CampaignType
import com.und.model.jpa.LiveSchedule
import com.und.model.jpa.Schedule
import java.time.LocalDateTime
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

class Campaign {
    var id: Long? = null

    @NotNull
    @Size(min = 2, max = 50)
    @Pattern(regexp = "[A-Za-z0-9-_][A-Za-z0-9-_\\s]*")
    var name: String = ""
    var schedule: Schedule? = null
    @NotNull
    lateinit var campaignType: CampaignType
    @NotNull
    var segmentationID: Long?=null
    //@NotNull
    var templateID: Long?= null
    var status: CampaignStatus? = null
    var dateCreated: LocalDateTime? = null
    var dateModified: LocalDateTime? = null
    var conversionEvent:String?=null
    var serviceProviderId:Long?=null
    var fromUser:String?=null
    var clientEmailSettingId:Long?=null
    var liveSchedule: LiveSchedule? = null
    var typeOfCampaign:TypeOfCampaign = TypeOfCampaign.NORMAL   //split,ab_test,normal
    var abCampaign:AbCampaign?=null
    var variants:List<Variant>? = null
}

enum class TypeOfCampaign {
    NORMAL,
    SPLIT,
    AB_TEST
}
