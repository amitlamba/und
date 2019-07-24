package com.und.repository.mongo

import com.und.web.model.CampaignReachedResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Repository

@Repository
class CampaignReachabilityRepositoryImpl:CampaignReachabilityRepository {

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    override fun getCampaignReachability(clientId:Long,campaignId:Long,campaignType:String): List<CampaignReachedResult> {
    var collectionName= getCollectionName(clientId,campaignType)
    var matchOperation=Aggregation.match(Criteria("campaignId").`is`(campaignId))
//    var unwindOperation=Aggregation.unwind("statusUpdates")
//    var groupOperation=Aggregation.group("statusUpdates.status").count().`as`("count")
//    var aggregation=Aggregation.newAggregation(matchOperation,unwindOperation,groupOperation)
        var groupOperation=Aggregation.group("status").count().`as`("count")
        var aggregation =Aggregation.newAggregation(matchOperation,groupOperation)
        return mongoTemplate.aggregate(aggregation,collectionName,CampaignReachedResult::class.java).mappedResults
    }
    private fun getCollectionName(clientId: Long,campaignType: String):String{
        when(campaignType){
            "EMAIL" -> return "${clientId}_email"
            "SMS" -> return "${clientId}_sms"
            "PUSH_ANDROID" -> return "${clientId}_fcmMessage"
            "PUSH_IOS" -> return "${clientId}_iosFcmMessage"
            "PUSH_WEB" -> return "${clientId}_webFcmMessage"
            else -> return ""
        }
    }
}