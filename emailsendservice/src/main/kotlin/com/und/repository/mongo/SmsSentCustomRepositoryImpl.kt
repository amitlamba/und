package com.und.repository.mongo

import com.und.model.mongo.Sms
import com.und.model.mongo.SmsStatus
import com.und.model.mongo.SmsStatusUpdate
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
class SmsSentCustomRepositoryImpl : SmsSentCustomRepository {


    @Autowired
    lateinit var mongoTemplate: MongoTemplate


    override fun saveSms(sms: Sms, clientId: Long) {
        mongoTemplate.insert(sms, "${clientId}_sms")
    }


    override fun updateStatus(smsId: String, smsStatus: SmsStatus, clientId: Long, clickTrackEventId: String?, message: String) {

        val query = Query(Criteria.where("clientID").`is`(clientId).and("_id").`is`(ObjectId(smsId)))
        val statusupdate = SmsStatusUpdate(LocalDateTime.now(ZoneId.of("UTC")), smsStatus, clickTrackEventId,message)
        val update = Update().push("statusUpdates", statusupdate).set("status", smsStatus)
        var v=mongoTemplate.findOne(query,Sms::class.java)
        mongoTemplate.updateFirst(query, update, Sms::class.java, "${clientId}_sms")
    }
}