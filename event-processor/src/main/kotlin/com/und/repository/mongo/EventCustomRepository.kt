package com.und.repository.mongo

import com.und.model.mongo.Event
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface EventCustomRepository {
    fun usersFromEvent(query: Aggregation, clientId: Long): List<String>
    fun usersFromEvent(aggregations: List<AggregationOperation>, clientId: Long): List<String>
    //fun findEventById(id: String, clientId: Long): Optional<Event>
   // fun findEventsListById(id: String, clientId: Long): List<Event>
    fun usersFromEvent(query: Query,clientId: Long):List<String>
   //fun findEventByObjectId(id:ObjectId,clientId: Long):Optional<Event>
    fun save(event:Event,clientId: Long):Event
    fun insertIfNotExists(event: Event,clientId: Long)
    fun insertIfNotExistsElseUpdate(event: Event,clientId: Long)
}