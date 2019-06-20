package com.und.repository.mongo

import com.und.model.SegmentUsers
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update

class CustomSegmentUsersRepositoryImpl:CustomSegmentUsersRepository {


    companion object {
        val logger = LoggerFactory.getLogger(CustomSegmentUsersRepositoryImpl::class.java)
    }
    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    override fun addUserInSegment(clientId: Long, userId: String, segmentId: Long) {

        val query = Query(Criteria.where("clientId").`is`(clientId).and("segmentId").`is`(segmentId))
        val update = Update().addToSet("users",userId)
        val result = mongoTemplate.updateFirst(query,update,SegmentUsers::class.java)
        if(result.modifiedCount>=1) logger.info("$userId is added in segment $segmentId users list .. clientId $clientId")
        else logger.info("$userId is already present in segment $segmentId users list .. clientId $clientId")
    }

    override fun removeUserFromSegment(clientId: Long, userId: String, segmentId: Long) {
        val query = Query(Criteria.where("clientId").`is`(clientId).and("segmentId").`is`(segmentId))
        val update = Update().pull("users",userId)
        val result = mongoTemplate.updateFirst(query,update,SegmentUsers::class.java)
        if(result.modifiedCount>=1) logger.info("$userId is removed from segment $segmentId users list .. clientId $clientId")
        else logger.info("$userId is not present in segment $segmentId users list .. clientId $clientId")
    }
}