package com.und.repository.mongo

import com.und.model.mongo.EventMetadata
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

class EventMetadataRepositoryImpl : EventMetadataCustomRepository {

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    private val clientIdCriteria: (clientId: Long) -> Query = { clientId -> Query.query(Criteria.where("clientId").`is`(clientId)) }

    override fun findAll(clientId: Long): List<EventMetadata> {
        return queryAEventMetadta(clientIdCriteria(clientId), mongoTemplate::find)
        //return mongoTemplate.find(,EventMetadata::class.java,"eventmetadata")
    }

    private fun <T> queryAbstract(query: Query, entity: Class<T>,
                                  collectionName: String,
                                  functi: (q: Query, entity: Class<T>, cName: String) -> List<T>): List<T> {
        return functi(query, entity, collectionName)
    }

    private fun queryAEventMetadta(query: Query,
                                   functi: (q: Query, entity: Class<EventMetadata>, cName: String) -> List<EventMetadata>): List<EventMetadata> {
        return queryAbstract<EventMetadata>(query, EventMetadata::class.java, "eventmetadata", functi)
    }
}