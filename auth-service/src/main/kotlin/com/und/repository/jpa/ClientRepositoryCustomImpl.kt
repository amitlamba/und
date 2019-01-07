package com.und.repository.jpa

import com.mongodb.BasicDBList
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.und.model.mongo.CommonMetadata
import com.und.model.mongo.EventMetadata
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate

class ClientRepositoryCustomImpl : ClientRepositoryCustom {

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    override fun saveUserProperties(clientId: Long, metadataJson: String) {

        val dbObject : DBObject = BasicDBObject.parse(metadataJson) as DBObject
        val parsed = dbObject.get("userProperties") as BasicDBList
        mongoTemplate.insert(parsed,  CommonMetadata::class.java)

    }

    override fun saveEventMetadta(clientId: Long, metadataJson: String) {
        val dbObject : DBObject = BasicDBObject.parse(metadataJson) as DBObject
        val parsed = dbObject.get("eventMetadata") as BasicDBList
        mongoTemplate.insert(parsed,  EventMetadata::class.java)

    }
}