package com.und.livesegment.repository.mongo

import org.junit.Test

import org.junit.Assert.*
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.ArrayOperators
import org.springframework.data.mongodb.core.query.Criteria
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class CustomLiveSegmentUserTrackRepositoryImplTest {

    @Test
    fun getLiveSegmentReportByDateRange() {
        var startDate1="2019-02-12"
        var endDate1="2019-03-02"
//        val startDate= LocalDate.parse(startDate1).atStartOfDay().atZone(ZoneId.systemDefault())
//        val endDate=LocalDate.parse(endDate1).atStartOfDay().plusDays(1).atZone(ZoneId.systemDefault())
//
//        println(Date.from(startDate.toInstant()))
//        println(Date.from(endDate.toInstant()))
        val startDate= Date.from(LocalDate.parse(startDate1).atStartOfDay().atZone(ZoneId.of("UTC")).toInstant())
        val endDate=Date.from(LocalDate.parse(endDate1).atStartOfDay().plusDays(1).atZone(ZoneId.of("UTC")).toInstant())

        val matchOperation= Aggregation.match(Criteria.where("segmentId").`is`(1).and("date").gt(startDate).lt(endDate))
        val projectOperation= Aggregation.project("userId").and("date").dateAsFormattedString("%Y-%m-%d").`as`("date")
        val groupOperation= Aggregation.group("date").count().`as`("totaluser").addToSet("userId").`as`("users")
        var size= ArrayOperators.Size.lengthOfArray("users")
        val projectOperation1= Aggregation.project("totaluser","users").and(size).`as`("uniqueuser").andExclude("_id")

        val aggregation= Aggregation.newAggregation(matchOperation,projectOperation,groupOperation,projectOperation1)

        println(aggregation)
    }
}