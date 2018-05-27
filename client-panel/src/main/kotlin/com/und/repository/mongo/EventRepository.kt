package com.und.repository.mongo

import com.und.model.mongo.eventapi.Event
import com.und.model.mongo.eventapi.Identity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface EventRepository : MongoRepository<Event, String>, EventCustomRepository {

    fun findByName(eventName: String): List<Event>
    fun findByIdentity(identity: Identity): List<Event>
}



