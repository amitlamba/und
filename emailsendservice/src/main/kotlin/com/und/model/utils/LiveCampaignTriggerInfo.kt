package com.und.model.utils

class LiveCampaignTriggerInfo {
    var campaignId:Long
    var clientId:Long
    var userId:String
    var templateId:Long?

    constructor(campaignId:Long,clientId:Long,userId:String,templateId:Long?){
        this.campaignId = campaignId
        this.clientId = clientId
        this.userId = userId
        this.templateId = templateId
    }
}