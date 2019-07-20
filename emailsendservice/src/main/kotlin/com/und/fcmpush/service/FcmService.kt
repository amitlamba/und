package com.und.fcmpush.service

import com.und.model.utils.FcmMessage
import com.und.model.utils.LiveCampaignTriggerInfo
import org.springframework.stereotype.Service

@Service
interface FcmService {

    fun sendMessage(message: FcmMessage)
    fun sendLiveMessage(infoModel:LiveCampaignTriggerInfo)
}