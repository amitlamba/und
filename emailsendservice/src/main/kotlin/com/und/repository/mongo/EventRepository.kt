package com.und.repository.mongo

import com.und.model.mongo.Event
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface EventRepository {
    fun findByName(eventName: String,clientId:Long): List<Event>
}



