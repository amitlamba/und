package com.und.repository.mongo

import com.und.model.mongo.eventapi.Event
import org.springframework.data.mongodb.core.aggregation.Aggregation
import java.util.*

interface EventCustomRepository {
    fun usersFromEvent(query: Aggregation, clientId: Long): List<String>
    fun findEventById(id: String, clientId: Long): Optional<Event>
    fun findEventsListById(id: String, clientId: Long): List<Event>

}