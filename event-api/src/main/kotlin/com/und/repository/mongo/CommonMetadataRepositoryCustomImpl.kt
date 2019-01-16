package com.und.repository.mongo

import com.und.model.mongo.eventapi.CommonMetadata
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import org.springframework.data.mongodb.core.query.Query.query





@Repository
class CommonMetadataRepositoryCustomImpl : CommonMetadataRepositoryCustom {

    @Autowired
    lateinit var mongoOperations :MongoOperations

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    override fun updateTechnographics(clientId:Long, technograhics: TechnoGraphics): CommonMetadata? {
        val name = "Technographics"
//        val query =  Query(Criteria.where("name").`is`(name))

        technograhics.properties.forEach{property ->
//            var n= property.name?.substring(0,1)?.toUpperCase()+property.name?.substring(1)
            val query =  Query(Criteria.where("name").`is`(name).and("properties.name").`is`(property.name))
//            val update :Update = Update().addToSet("properties.${property.name}.options", property.options)
            val update :Update = Update().addToSet("properties.$.options").each(property.options)
            mongoOperations.updateFirst(query, update, "${clientId}_userproperties")

        }
        return technograhics
    }

    override fun updateAppFields(clientId:Long,appFields: AppFields): CommonMetadata? {
        val name = "AppFields"
//        val query =  Query(Criteria.where("name").`is`(name))
        appFields.properties.forEach{property ->
            var n= property.name?.substring(0,1)?.toUpperCase()+property.name?.substring(1)
            val query =  Query(Criteria.where("name").`is`(name).and("properties.name").`is`(n))
//            val update :Update = Update().addToSet("properties.${property.name}.options", property.options)
            val update :Update = Update().addToSet("properties.$.options").each(property.options)
            mongoOperations.updateFirst(query, update, "${clientId}_userproperties")

        }
        return appFields
    }
}



