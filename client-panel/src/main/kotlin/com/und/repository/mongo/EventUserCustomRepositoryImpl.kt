package com.und.repository.mongo

import com.und.model.mongo.eventapi.EventUser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria

class EventUserCustomRepositoryImpl : EventUserCustomRepository {

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    override fun findUserById(id: String, clientId: Long): EventUser? {
        val q = Query(Criteria.where("_id").`is`(id))
        return mongoTemplate.findOne(q, EventUser::class.java, "${clientId}_eventUser")

    }
}