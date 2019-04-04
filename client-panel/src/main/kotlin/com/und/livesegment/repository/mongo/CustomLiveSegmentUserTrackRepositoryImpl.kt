package com.und.livesegment.repository.mongo

import com.und.livesegment.model.mongo.UserCount
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.ArrayOperators
import org.springframework.data.mongodb.core.aggregation.DateOperators
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

class CustomLiveSegmentUserTrackRepositoryImpl:CustomLiveSegmentUserTrackRepository {

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    override fun findCountByClientIdAndLiveSegmentId(clientId: Long, liveSegmentId: Long): Long {
        val matchOperation=Aggregation.match(Criteria.where("clientID").`is`(clientId).and("liveSegmentId").`is`(liveSegmentId))
        val countOperation=Aggregation.count().`as`("count")
        val query=Aggregation.newAggregation(matchOperation,countOperation)
        val result= mongoTemplate.aggregate(query,"${clientId}_livesegmenttrack",UserCount::class.java).mappedResults
        return result[0].count
    }

    override fun findCountByClientIdAndSegmentId(clientId: Long, segmentId: Long): Pair<Long,Long> {
        val matchOperation=Aggregation.match(Criteria.where("clientID").`is`(clientId).and("segmentId").`is`(segmentId))
        val groupOperation=Aggregation.group("userIdentified").count().`as`("count")
        val query=Aggregation.newAggregation(matchOperation,groupOperation)
        val result= mongoTemplate.aggregate(query,"${clientId}_livesegmenttrack",UserCount::class.java).mappedResults
        return modifyResult(result)
    }

    override fun getLiveSegmentReportByDateRange(startDate: String, endDate: String, clientId: Long,segmentId: Long):List<LiveSegmentResult> {
        val startDate= Date.from(LocalDate.parse(startDate).atStartOfDay().atZone(ZoneId.of("UTC")).toInstant())
        val endDate=Date.from(LocalDate.parse(endDate).atStartOfDay().plusDays(1).atZone(ZoneId.of("UTC")).toInstant())

        val matchOperation=Aggregation.match(Criteria.where("segmentId").`is`(segmentId).and("clientID").`is`(clientId).and("date").gt(startDate).lt(endDate))
        val projectOperation=Aggregation.project("userId").and("date").dateAsFormattedString("%Y-%m-%d").`as`("date")
        val groupOperation=Aggregation.group("date").count().`as`("totalusersperday").addToSet("userId").`as`("users")
        var size=ArrayOperators.Size.lengthOfArray("users")
        val projectOperation1=Aggregation.project("totalusersperday","users").and(size).`as`("uniqueusersperday").and("_id").`as`("date")

        val aggregation=Aggregation.newAggregation(matchOperation,projectOperation,groupOperation,projectOperation1)

        return mongoTemplate.aggregate(aggregation,"${clientId}_livesegmenttrack",LiveSegmentResult::class.java).mappedResults

    }

    private fun modifyResult(result:MutableList<UserCount>):Pair<Long,Long>{
        //check what happen if result is o
        if(result.isNotEmpty()){
            var identified=0L
            var notIdentified=0L
            for(i in 0 until (result.size) step 1){
                result[i]._id?.let {
                    if(it) identified = result[i].count
                    else notIdentified = result[i].count
                }
            }
            return Pair(identified,notIdentified)
        }else return Pair(0,0)
    }
}

data class LiveSegmentResult(val totalusersperday:Int,val uniqueusersperday:Int,val users:List<String>,val date:String)