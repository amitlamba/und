package com.und.report.repository.mongo

import com.und.common.utils.loggerFor
import com.und.report.model.AggregateOutput
import com.und.service.AGGREGATE_VALUE
import com.und.service.SegmentParserCriteria
import org.bson.Document
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.stereotype.Repository

@Repository
class UserAnalyticsRepositoryImpl: UserAnalyticsRepository{

    companion object {
        val logger: Logger = loggerFor(UserAnalyticsRepositoryImpl::class.java)
    }

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    override fun aggregate(query: Aggregation, clientId: Long): List<AggregateOutput> {
        logger.debug("Fetching aggregation results for query : $query, clientId: $clientId")

        val aggregate = mongoTemplate.aggregate<Document>(query, "${clientId}_event", Document::class.java)

        SegmentParserCriteria.logger.debug("Total ${aggregate.mappedResults.size} results found")

        if(aggregate.mappedResults.size == 0) return emptyList()

        val firstDocument = aggregate.mappedResults.get(0)

        if(firstDocument["_id"] is String)
            return aggregate.mappedResults.map { document -> AggregateOutput(mapOf("name" to document["_id"].toString()), document[AGGREGATE_VALUE].toString().toDouble()) }
        else
            return aggregate.mappedResults.map { document -> AggregateOutput(document.filter {it.key != AGGREGATE_VALUE}, document[AGGREGATE_VALUE].toString().toDouble()) }
    }


}