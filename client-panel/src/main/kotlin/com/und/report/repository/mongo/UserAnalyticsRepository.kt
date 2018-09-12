package com.und.report.repository.mongo

import com.und.report.model.AggregateOutput
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.stereotype.Repository

@Repository
interface UserAnalyticsRepository {

    fun aggregate(query: Aggregation, clientId: Long): List<AggregateOutput>

}