package com.und.repository.mongo

import com.und.model.mongo.SegmentReachability
import com.und.service.SegmentResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import java.time.LocalDateTime
import java.util.*

class CustomSegmentReachabilityRepositoryImpl:CustomSegmentReachabilityRepository {

    @Autowired
    private lateinit var mongoTemplate:MongoTemplate

    override fun updateSegmentReachability(segmentId:Long,key:String,count:Map<String,Int>,clientID:Long,modifiedTime:LocalDateTime,timezone:String) {
        //TODO create a segmentreachability document when a segment created. we can use this to update a single hash key on specific date
        val exist=mongoTemplate.exists(Query(Criteria.where("_id").`is`(segmentId).and("clientId").`is`(clientID)),"segmentreachability")
        if(exist) {
            mongoTemplate.updateFirst(Query(Criteria.where("_id").`is`(segmentId).and("clientId").`is`(clientID)), Update.update(key, count).set("lastModifiedTime",modifiedTime), "segmentreachability")
        }else {
            val reachability=SegmentReachability()
            with(reachability){
                id=segmentId
                clientId=clientID
                dates= mutableMapOf(Pair(key.split(".")[1].toInt(),count))
                lastModifiedTime=modifiedTime
                timeZone=timezone
            }
            mongoTemplate.save(reachability,"segmentreachability")
        }
    }

    override fun getReachabilityOfSegmentByDate(segmentId: Long,key: String,date:String,clientId: Long): Map<String,Int> {
        var match= Aggregation.match(Criteria("_id").`is`(segmentId).and("clientId").`is`(clientId))
        var project1= Aggregation.project(key).andExclude("_id")
        var project2= Aggregation.project().and(date.replace("-","")).`as`("key")
        var agg= Aggregation.newAggregation(match,project1,project2)
        return mongoTemplate.aggregate(agg,"segmentreachability", SegmentResult::class.java).mappedResults[0].key
    }

    override fun updateAllUsersSegmentReachability(segmentId: Long, key: String, count: Map<String, Int>, clientId: Long, modifiedTime: LocalDateTime, timeZone: String) {
        //TODO create a segmentreachability document when a segment created.
        val exist=mongoTemplate.exists(Query(Criteria.where("_id").`is`(segmentId).and("clientId").`is`(clientId)),"segmentreachability")
        if(exist) {
            mongoTemplate.updateFirst(Query(Criteria.where("_id").`is`(segmentId).and("clientId").`is`(clientId)), Update.update(key, count).set("lastModifiedTime",modifiedTime), "segmentreachability")
        }else {
            val reachability=SegmentReachability()
            with(reachability){
                id=segmentId
                this.clientId=clientId
                dates= mutableMapOf(Pair(key.split(".")[1].toInt(),count))
                lastModifiedTime=modifiedTime
                this.timeZone=timeZone
            }
            mongoTemplate.save(reachability,"segmentreachability")
        }
    }
}