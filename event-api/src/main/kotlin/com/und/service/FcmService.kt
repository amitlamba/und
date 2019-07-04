package com.und.service

import com.und.config.EventStream
import com.und.model.NotificationRead
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service

@Service
class FcmService {


    @Autowired
    private lateinit var eventStream: EventStream

    fun updateStatus(mongoId: String,clientId: Long,type:String){
        var message= NotificationRead(mongoId=mongoId,clientId = clientId,type = type)
        toKafka(message)
    }

    fun toKafka(message: NotificationRead){
        eventStream.outNotificationRead().send(MessageBuilder.withPayload(message).build())
    }
}


