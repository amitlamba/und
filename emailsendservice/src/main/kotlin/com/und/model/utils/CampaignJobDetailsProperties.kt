package com.und.model.utils

import javax.validation.constraints.NotBlank

class CampaignJobDetailProperties: JobDetailProperties() {

    @NotBlank
    lateinit var campaignId: String

    @NotBlank
    var campaignName: String? = null

    var abCompleted:String?=null

}