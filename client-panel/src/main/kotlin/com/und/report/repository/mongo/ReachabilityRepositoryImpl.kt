package com.und.report.repository.mongo

import com.und.report.web.model.Reachability
import com.und.report.web.model.ReachabilityResult
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.FacetOperation
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Repository

@Repository
class ReachabilityRepositoryImpl :ReachabilityRepository{

    @Autowired
    private lateinit var mongoTemplate:MongoTemplate

    override fun getReachabilityOfSegment(clientId:Long,segmentId: Long, segmentUsers: List<ObjectId>):ReachabilityResult {

        var matchOperation=Aggregation.match(Criteria("_id").`in`(segmentUsers))

        var facetOperation=Aggregation.facet()
                .and(
                        Aggregation.match(Criteria().norOperator(Criteria("identity.email").`is`(null),Criteria("communication.email.dnd").`in`(true,null))),
                        Aggregation.count().`as`("count")).`as`("email")
                .and(
                        Aggregation.match(Criteria().norOperator(Criteria("identity.mobile").`is`(null),Criteria("communication.mobile.dnd").`in`(true,null))),
                        Aggregation.count().`as`("count")).`as`("mobile")
                .and(
                        Aggregation.match(Criteria().norOperator(Criteria("identity.androidFcmToken").`is`(null),Criteria("communication.android.dnd").`in`(true,null))),
                        Aggregation.count().`as`("count")).`as`("android")
                .and(
                        Aggregation.match(Criteria().norOperator(Criteria("identity.iosFcmToken").`is`(null),Criteria("communication.ios.dnd").`in`(true,null))),
                        Aggregation.count().`as`("count")).`as`("ios")
                .and(
                        Aggregation.match(Criteria().norOperator(Criteria("identity.webFcmToken").`is`(null),Criteria("communication.webpush.dnd").`in`(true,null))),
                        Aggregation.count().`as`("count")).`as`("webpush")

        var projectionOperation=Aggregation.project()
                .and("email.count").`as`("emailCount")
                .and("mobile.count").`as`("mobileCount")
                .and("android.count").`as`("androidCount")
                .and("ios.count").`as`("iosCount")
                .and("webpush.count").`as`("webCount")

        var aggregation:Aggregation= Aggregation.newAggregation(matchOperation,facetOperation,projectionOperation)
       return mongoTemplate.aggregate(aggregation,"${clientId}_eventUser", ReachabilityResult::class.java).mappedResults[0]
    }
}