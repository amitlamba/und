package com.und.repository.jpa

import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.und.model.mongo.CommonMetadata
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

class ClientRepositoryCustomImpl : ClientRepositoryCustom {

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    @Autowired
    lateinit var mongoOperations: MongoOperations

    override fun saveUserProperties(clientId: Long, metadataJson: String) {

        val dbObject: DBObject = BasicDBObject.parse(metadataJson) as DBObject
        val parsed = dbObject.get("userProperties") as BasicDBList
        mongoTemplate.insert(parsed, "${clientId}_userproperties")

    }

    override fun saveEventMetadta(clientId: Long, metadataJson: String) {
        val dbObject: DBObject = BasicDBObject.parse(metadataJson) as DBObject
        val parsed = dbObject.get("eventMetadata") as BasicDBList
        mongoTemplate.insert(parsed, "${clientId}_eventmetadata")

    }

    override fun userpropertiesExists(clientId: Long): Boolean {
        return mongoOperations.exists(Query(Criteria.where("name").`is`("Technographics").exists(true)), "${clientId}_userproperties")
    }

    override fun eventMetadtaExists(clientId: Long): Boolean {
        return mongoOperations.exists(Query(Criteria.where("name").exists(true)), "${clientId}_eventmetadata")
    }
}