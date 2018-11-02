package com.und.kafkalisterner

import com.und.model.utils.FcmMessage
import com.und.service.FcmSendService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.stereotype.Service

@Service
class FcmListner {

    @Autowired
    private lateinit var fcmSendService:FcmSendService

    @StreamListener("fcmEventSend")
    fun sendMessage(message: FcmMessage){
        fcmSendService.sendMessage(message)
    }
}