package com.und.repository.mongo

import com.und.model.mongo.Event
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

class EventRepositoryImpl:EventRepository {
    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    override fun findByName(eventName: String,clientId:Long): List<Event> {
       return mongoTemplate.find(Query.query(Criteria.where("name").`is`(eventName).and("clientId").`is`(clientId)),Event::class.java,"${clientId}_event")
    }
}