package com.und.service

import com.und.config.EventStream
import com.und.model.FcmMessageStatus
import com.und.model.FcmMessageUpdates
import com.und.model.NotificationRead
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.Output
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class AndroidService {

    @Autowired
    private lateinit var eventStream: EventStream

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    fun updateStatus(mongoId: String, clientId: Long) {
        toKafka(NotificationRead(mongoId = mongoId, clientId = clientId))
    }

    private fun toKafka(message: NotificationRead) {
        var message = MessageBuilder.withPayload(message).build()
        eventStream.outNotificationRead().send(message)
    }

    @StreamListener("inNotificationRead")
    private fun listeningNotificationUpdateStatus(message: NotificationRead) {
        var criteria = Criteria("_id").`in`(ObjectId(message.mongoId))
        var query = Query().addCriteria(criteria)
        var update = Update().push("statusUpdates", FcmMessageUpdates(LocalDateTime.now(ZoneId.of("UTC")), FcmMessageStatus.READ)).set("status", FcmMessageStatus.READ)
        mongoTemplate.updateFirst(query, update, "${message.clientId}_fcmMessage")
    }

}