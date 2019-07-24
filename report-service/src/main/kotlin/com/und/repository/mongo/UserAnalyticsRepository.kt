package com.und.repository.mongo

import com.und.model.AggregateOutput
import com.und.model.UserData
import com.und.web.model.Reachability
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.stereotype.Repository

@Repository
interface UserAnalyticsRepository {

    fun aggregate(query: Aggregation, clientId: Long): List<AggregateOutput>

    fun getReachability(query: Aggregation,clientId: Long):Reachability

    fun funnelData(query: Aggregation, clientId: Long): List<UserData>

}