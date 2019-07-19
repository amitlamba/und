package com.und.fcmpush.listner

import com.und.model.utils.FcmMessage
import com.und.fcmpush.service.FcmService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.stereotype.Service

@Service
class FcmListner {

    @Autowired
    private lateinit var fcmSendService: FcmService

    //TODO make reties 0
    @StreamListener("fcmEventReceive")
    fun sendMessage(message: FcmMessage) {
        fcmSendService.sendMessage(message)
    }
}