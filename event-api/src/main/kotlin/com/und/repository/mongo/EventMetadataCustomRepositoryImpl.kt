//package com.und.repository.mongo
//
//import com.und.model.mongo.eventapi.EventMetadata
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.data.mongodb.core.MongoTemplate
//import org.springframework.data.mongodb.core.findOne
//import org.springframework.data.mongodb.core.query.Criteria
//import org.springframework.data.mongodb.core.query.Query
//
//class EventMetadataCustomRepositoryImpl:EventMetadataCustomRepository {
//    @Autowired
//    private lateinit var mongoTemplate: MongoTemplate
//
//    override fun findByName(name: String, clientId: Long): EventMetadata? {
//        return mongoTemplate.findOne(Query.query(Criteria.where("name").`is`(name).and("clientId").`is`(clientId)),"eventmetadata")
//    }
//
//    override fun save(eventMetadata: EventMetadata, clientId: Long) {
//        mongoTemplate.save(eventMetadata,"eventmetadata")
//    }
//
//    override fun findAll(clientId: Long): List<EventMetadata> {
//        val query = Query.query(Criteria.where("clientId").`is`(clientId))
//        return mongoTemplate.find(query, EventMetadata::class.java,"eventmetadata")
//    }
//}