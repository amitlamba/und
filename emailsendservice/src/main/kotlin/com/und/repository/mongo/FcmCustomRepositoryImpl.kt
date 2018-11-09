package com.und.repository.mongo

import com.und.model.mongo.AnalyticFcmMessage
import com.und.model.mongo.EmailStatusUpdate
import com.und.model.mongo.FcmMessageStatus
import com.und.model.mongo.FcmMessageUpdates
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.ZoneId

@Repository
class FcmCustomRepositoryImpl:FcmCustomRepository {

    @Autowired
    private lateinit var mongoTemplate:MongoTemplate

    override fun saveAnalyticMessage(message: AnalyticFcmMessage, clientId: Long) {
        mongoTemplate.save(message,"${clientId}_fcmMessage")
    }

    override fun updateStatus(mongoId:String,status:FcmMessageStatus,clientId: Long,clickTrackEventId: String?) {
        val query = Query(Criteria.where("clientId").`is`(clientId).and("_id").`is`(ObjectId(mongoId)))
        val statusupdate = FcmMessageUpdates(LocalDateTime.now(ZoneId.of("UTC")), status,clickTrackEventId)
        val update = Update().push("statusUpdates", statusupdate).set("fcmStatus", status)
        mongoTemplate.updateFirst(query, update, AnalyticFcmMessage::class.java, "${clientId}_fcmMessage")
    }
}