package com.und.repository.mongo

import com.und.model.mongo.Email
import com.und.model.mongo.EmailStatus
import com.und.model.mongo.EmailStatusUpdate
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
class EmailSentCustomRepositoryImpl : EmailSentCustomRepository {

    @Autowired
    lateinit var mongoTemplate: MongoTemplate


    override fun saveEmail(email: Email, clientId: Long) {
        mongoTemplate.insert(email, "${clientId}_email")
    }


    override fun updateStatus(emailId: String, emailStatus: EmailStatus, clientId: Long, clickTrackEventId: String?) {

        val query = Query(Criteria.where("clientID").`is`(clientId).and("_id").`is`(ObjectId(emailId)))
            val statusupdate = EmailStatusUpdate(LocalDateTime.now(), emailStatus, clickTrackEventId)
            val update = Update().push("statusUpdates", statusupdate).set("emailStatus", emailStatus)
            mongoTemplate.updateFirst(query, update, Email::class.java, "${clientId}_email")
    }

}