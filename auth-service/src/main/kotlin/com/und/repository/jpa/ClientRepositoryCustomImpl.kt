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
import org.springframework.data.mongodb.core.query.isEqualTo

class ClientRepositoryCustomImpl : ClientRepositoryCustom {

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    @Autowired
    lateinit var mongoOperations: MongoOperations

    override fun saveUserProperties(clientId: Long, metadataJson: List<CommonMetadata>) {

//        val dbObject: DBObject = BasicDBObject.parse(metadataJson) as DBObject
//        val parsed = dbObject.get("userProperties") as BasicDBList
//        mongoTemplate.insert(parsed, "userproperties")
        mongoTemplate.insert(metadataJson, "userproperties")

    }

    override fun saveEventMetadta(clientId: Long, metadataJson: String) {
        val dbObject: DBObject = BasicDBObject.parse(metadataJson) as DBObject
        val parsed = dbObject.get("eventMetadata") as BasicDBList
        mongoTemplate.insert(parsed, "eventmetadata")

    }

    override fun userpropertiesExists(clientId: Long): Boolean {

//        var query=Query(Criteria.where("name").exists(true).`is`("Technographics"))
        return mongoOperations.exists(Query.query(Criteria.where("clientId").`is`(clientId)), "userproperties")
        //return mongoOperations.collectionExists("userproperties")
//        return mongoOperations.exists(query, "${clientId}_userproperties")

    }

    override fun eventMetadtaExists(clientId: Long): Boolean {
        return mongoOperations.exists(Query(Criteria.where("name").exists(true)), "eventmetadata")
    }

    override fun getCommonUserProperties(): List<CommonMetadata> {
        // -1 is clientId for commonmetadata
        val query = Query.query(Criteria.where("clientId").`is`(-1))
        return mongoTemplate.find(query, CommonMetadata::class.java, "userproperties")
    }
}