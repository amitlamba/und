package com.und.repository.mongo

import org.bson.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation


class EventCustomRepositoryImpl : EventCustomRepository {

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    override fun usersFromEvent(query: Aggregation, clientId: Long): List<String> {

        val output = mongoTemplate.aggregate(query, "${clientId}_event", Document::class.java)
        return if(output != null) {
            output.mapNotNull { dbo -> dbo["_id"] as String}
        } else emptyList()

    }
}