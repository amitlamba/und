package com.und.repository.mongo

import com.und.model.mongo.eventapi.Event
import org.bson.Document
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import java.util.*


class EventCustomRepositoryImpl : EventCustomRepository {

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    override fun usersFromEvent(query: Aggregation, clientId: Long): List<String> {
        val output = mongoTemplate.aggregate(query, "${clientId}_event", Document::class.java)
        return output?.let { aggResult ->
            aggResult.mapNotNull { dbo -> dbo["_id"].toString()}
        } ?: emptyList()
    }

    override fun usersFromEvent(aggregations: List<AggregationOperation>, clientId: Long): List<String> {
        if(aggregations.isNotEmpty()){
            val agg=Aggregation.newAggregation(aggregations)
            val output = mongoTemplate.aggregate(agg, "${clientId}_event", Document::class.java)
            return output.mappedResults.filter {
                it["_id"] !=null
            }.map { it["_id"] as String }
        }
        return emptyList()
    }

    override fun usersFromEvent(query: Query, clientId: Long): List<String> {
        val output = mongoTemplate.find(query, Event::class.java,"${clientId}_event")
        return output?.let { aggResult ->
            aggResult.mapNotNull { event ->event.userId  as String }
        } ?: emptyList()
    }

    override fun findEventById(id: String, clientId: Long): Optional<Event> {
        val q = Query(Criteria.where("_id").`is`(id))
        return queryEvent(q, clientId)
    }

    override fun findEventsListById(id: String, clientId: Long): List<Event>{
        val q = Query(Criteria.where("userId").`is`(id))
        return queryEventsList(q, clientId)
    }

    private fun queryEvent(q: Query, clientId: Long): Optional<Event> {
        val eventDetails = mongoTemplate.findOne(q, Event::class.java, "${clientId}_event")
        return if (eventDetails == null) {
            Optional.empty()
        } else {
            Optional.of(eventDetails)
        }
    }

    private fun queryEventsList(q: Query, clientId: Long): List<Event> {
        val eventList = mongoTemplate.find(q, Event::class.java, "${clientId}_event")
        return eventList
    }
}