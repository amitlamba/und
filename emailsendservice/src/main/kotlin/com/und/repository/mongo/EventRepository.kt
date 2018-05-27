package com.und.repository.mongo

import com.und.model.mongo.Event
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface EventRepository : MongoRepository<Event, String> {

    fun findByName(eventName: String): List<Event>

}



