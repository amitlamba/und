package com.und.repository.mongo

import com.und.report.service.SegmentResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update

class CustomSegmentReachabilityRepositoryImpl:CustomSegmentReachabilityRepository {

    @Autowired
    private lateinit var mongoTemplate:MongoTemplate

    override fun updateSegmentReachability(segmentId:Long,key:String,objectIds:Int) {
        mongoTemplate.updateFirst(Query(Criteria.where("_id").`is`(segmentId)), Update.update(key,objectIds),"segmentreachability")
    }

    override fun getReachabilityOfSegmentByDate(segmentId: Long,key: String,date:String): Int {
        var match= Aggregation.match(Criteria("_id").`is`(segmentId))
        var project1= Aggregation.project(key).andExclude("_id")
        var project2= Aggregation.project().and(date.replace("-","")).`as`("key")
        var agg= Aggregation.newAggregation(match,project1,project2)
        return mongoTemplate.aggregate(agg,"segmentreachability", SegmentResult::class.java).mappedResults[0].key
    }
}