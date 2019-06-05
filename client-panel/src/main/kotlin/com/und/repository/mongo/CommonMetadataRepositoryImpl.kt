package com.und.repository.mongo

import com.und.model.mongo.CommonMetadata
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

class CommonMetadataRepositoryImpl:CommonMetadataCustomRepository {

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    private val clientIdCriteria: (clientId: Long) -> Query = { clientId -> Query.query(Criteria.where("clientId").`is`(clientId)) }

    override fun findAll(clientId: Long): List<CommonMetadata> {
        return queryAEventMetadta(clientIdCriteria(clientId), mongoTemplate::find)
        //return mongoTemplate.find(,EventMetadata::class.java,"eventmetadata")
    }

    private fun <T> queryAbstract(query: Query, entity: Class<T>,
                                  collectionName: String,
                                  functi: (q: Query, entity: Class<T>, cName: String) -> List<T>): List<T> {
        return functi(query, entity, collectionName)
    }

    private fun queryAEventMetadta(query: Query,
                                   functi: (q: Query, entity: Class<CommonMetadata>, cName: String) -> List<CommonMetadata>): List<CommonMetadata> {
        return queryAbstract<CommonMetadata>(query, CommonMetadata::class.java, "userproperties", functi)
    }


}