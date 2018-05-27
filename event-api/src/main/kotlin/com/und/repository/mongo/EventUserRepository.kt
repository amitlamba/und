package com.und.repository.mongo

import com.und.model.mongo.eventapi.EventUser
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface EventUserRepository : MongoRepository<EventUser, String> {


    override fun findById(id: String): Optional<EventUser>

}