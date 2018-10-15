package com.und.model

import javax.validation.constraints.NotBlank

class CampaignJobDetailProperties: JobDetailProperties() {

    @NotBlank
    lateinit var campaignId: String

    @NotBlank
    var campaignName: String? = null

}