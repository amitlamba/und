//package com.und.repository.mongo
//
//import com.und.model.mongo.eventapi.EventMetadata
//import org.springframework.data.mongodb.core.findOne
//import org.springframework.data.mongodb.core.query.Criteria
//import org.springframework.data.mongodb.core.query.Query
//
//interface EventMetadataCustomRepository {
//
//     fun findByName(name: String, clientId: Long): EventMetadata?
//
//     fun save(eventMetadata: EventMetadata, clientId: Long)
//
//     fun findAll(clientId: Long): List<EventMetadata>
//}