package com.und.service

import com.und.config.EventStream
import com.und.model.FcmMessageStatus
import com.und.model.FcmMessageUpdates
import com.und.model.NotificationRead
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class FcmService {

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var eventStream: EventStream

    fun updateStatus(mongoId: String,clientId: Long,type:String){
        var message= NotificationRead(mongoId=mongoId,clientId = clientId,type = type)
        toKafka(message)
    }

    fun toKafka(message: NotificationRead){
        eventStream.outNotificationRead().send(MessageBuilder.withPayload(message).build())
    }
    @StreamListener("inNotificationRead")
    fun listeningWebpushNotification(message: NotificationRead){
        var query= Query.query(Criteria("_id").`is`(ObjectId(message.mongoId)))
        var update= Update().push("statusUpdates", FcmMessageUpdates(LocalDateTime.now(), FcmMessageStatus.READ))
                .set("status", FcmMessageStatus.READ)
        mongoTemplate.updateFirst(query,update,resolveFcmCollectionName(message.clientId,message.type))
    }

    private fun resolveFcmCollectionName(clientId: Long,type: String):String{
        when(type){
            "android" -> return "${clientId}_fcmMessage"
            "web" -> return "${clientId}_webFcmMessage"
            "ios" -> return "${clientId}_iosFcmMessage"
            else -> return ""
        }
    }
}