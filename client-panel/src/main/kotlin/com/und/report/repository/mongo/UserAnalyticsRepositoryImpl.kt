//package com.und.report.repository.mongo
//
//import com.und.common.utils.loggerFor
//import com.und.report.model.AggregateOutput
//import com.und.report.model.UserData
//import com.und.report.web.model.Reachability
//import com.und.service.AGGREGATE_VALUE
//import com.und.service.SegmentParserCriteria
//import org.bson.Document
//import org.slf4j.Logger
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.data.mongodb.core.MongoTemplate
//import org.springframework.data.mongodb.core.aggregation.Aggregation
//import org.springframework.stereotype.Repository
//
//@Repository
//class UserAnalyticsRepositoryImpl: UserAnalyticsRepository{
//    companion object {
//        val logger: Logger = loggerFor(UserAnalyticsRepositoryImpl::class.java)
//    }
//
//    @Autowired
//    lateinit var mongoTemplate: MongoTemplate
//
////    override fun aggregate(query: Aggregation, clientId: Long): List<AggregateOutput> {
////        logger.debug("Fetching aggregation results for query : $query, clientId: $clientId")
////
////        val aggregate = mongoTemplate.aggregate<Document>(query, "${clientId}_event", Document::class.java)
////        val result=if(aggregate.mappedResults.isNotEmpty() && (aggregate.mappedResults[0]["_id"] !=null || aggregate.mappedResults[0].containsKey("_id")))
////        {
////            aggregate.mappedResults.filter {it["_id"] != null }
////        }
////        else {
////            aggregate.mappedResults
////        }
////        logger.debug("Total ${aggregate.mappedResults.size} results found")
////
////        if(aggregate.mappedResults.size == 0) return emptyList()
////
////        val firstDocument = aggregate.mappedResults[0]
////
////        if(firstDocument["_id"] is String)
////            return aggregate.mappedResults.map { document -> AggregateOutput(mapOf("name" to document["_id"].toString()), document[AGGREGATE_VALUE].toString().toDouble()) }
////        else
////            return aggregate.mappedResults.map { document -> AggregateOutput(document.filter {it.key != AGGREGATE_VALUE}, document[AGGREGATE_VALUE].toString().toDouble()) }
////        logger.debug("Total ${result.size} results found")
////
////        if(result.isEmpty()) return emptyList()
////
////        val firstDocument = result[0]
////
////        if(firstDocument["_id"] is String)
////            return result.map { document -> AggregateOutput(mapOf("name" to document["_id"].toString()), document[AGGREGATE_VALUE].toString().toDouble()) }
////        else
////            return result.map { document -> AggregateOutput(document.filter {it.key != AGGREGATE_VALUE}, document[AGGREGATE_VALUE].toString().toDouble()) }
////
////    }
//
////    override fun getReachability(query: Aggregation, clientId: Long): Reachability {
////        var result= mongoTemplate.aggregate(query,"${clientId}_event",Reachability::class.java)
////        return if(result.mappedResults.isNotEmpty()) result.mappedResults[0] else Reachability()
////    }
//
//    override fun funnelData(query: Aggregation, clientId: Long): List<UserData> {
//        logger.debug("Fetching funnelData results for query : $query, clientId: $clientId")
//
//        val aggregate = mongoTemplate.aggregate<UserData>(query, "${clientId}_event", UserData::class.java)
//
//        logger.debug("Total ${aggregate.mappedResults.size} results found")
//
//        return aggregate.mappedResults
//    }
//}