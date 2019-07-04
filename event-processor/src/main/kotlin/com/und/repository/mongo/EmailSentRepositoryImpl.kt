package com.und.repository.mongo

import com.und.model.Email
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import java.util.*

class EmailSentCustomRepositoryImpl:EmailSentCustomRepository {

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    override fun saveEmail(email: Email): Email? {
        return mongoTemplate.save(email,"${email.clientID}_email")
    }

    override fun findById(mongoId: String?, clientId: Long): Optional<Email> {
     val email = mongoTemplate.findById(mongoId,Email::class.java,"${clientId}_email")
        return if (email==null) Optional.empty() else Optional.of(email)
    }
}