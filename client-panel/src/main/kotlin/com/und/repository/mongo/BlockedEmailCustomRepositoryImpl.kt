package com.und.repository.mongo

import com.und.model.mongo.BlockedEmail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update

class BlockedEmailCustomRepositoryImpl : BlockedEmailCustomRepository {

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    override fun appendHistory(clientId: Long, blockedEmail: BlockedEmail) {
        val query = Query(where("clientId").`is`(clientId))
        val blockedEmailPersisted = mongoTemplate.find(query, BlockedEmail::class.java)
        if (blockedEmailPersisted == null || blockedEmailPersisted.isEmpty()) {
            mongoTemplate.save(blockedEmail)
        } else {
            val update = Update().pushAll("history", blockedEmail.history.toTypedArray())
            mongoTemplate.updateFirst(query, update, BlockedEmail::class.java)
        }

    }

}



