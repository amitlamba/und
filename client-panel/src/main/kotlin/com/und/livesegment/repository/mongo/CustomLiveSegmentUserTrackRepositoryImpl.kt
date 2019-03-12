package com.und.livesegment.repository.mongo

import com.und.livesegment.model.mongo.UserCount
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

class CustomLiveSegmentUserTrackRepositoryImpl:CustomLiveSegmentUserTrackRepository {

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    override fun findCountByClientIdAndLiveSegmentId(clientId: Long, liveSegmentId: Long): Long {
        val matchOperation=Aggregation.match(Criteria.where("clientId").`is`(clientId).and("liveSegmentId").`is`(liveSegmentId))
        val countOperation=Aggregation.count().`as`("count")
        val query=Aggregation.newAggregation(matchOperation,countOperation)
        val result= mongoTemplate.aggregate(query,"${clientId}_livesegmenttrack",UserCount::class.java).mappedResults
        return result[0].count
    }

    override fun findCountByClientIdAndSegmentId(clientId: Long, segmentId: Long): Long {
        val matchOperation=Aggregation.match(Criteria.where("clientId").`is`(clientId).and("segmentId").`is`(segmentId))
        val countOperation=Aggregation.count().`as`("count")
        val query=Aggregation.newAggregation(matchOperation,countOperation)
        val result= mongoTemplate.aggregate(query,"${clientId}_livesegmenttrack",UserCount::class.java).mappedResults
        return result[0].count
    }
}