package com.und.service

import com.und.report.web.model.EventReport
import com.und.report.web.model.GroupBy
import com.und.web.model.GlobalFilter
import com.und.web.model.GlobalFilterType
import com.und.web.model.StringOperator
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.test.util.ReflectionTestUtils
import java.time.ZoneId

@RunWith(MockitoJUnitRunner::class)
class MongoQueryUtilTest {


    private lateinit var segmentParserCriteria: SegmentParserCriteria

    private lateinit var mongoQueryUtil: MongoQueryUtil

    @Before
    fun setup(){
        segmentParserCriteria= SegmentParserCriteria()
        mongoQueryUtil = MongoQueryUtil()
        ReflectionTestUtils.setField(mongoQueryUtil, "segmentParserCriteria", segmentParserCriteria)
    }

    @Test
    fun testLiveUser1(){
        val groupBy = GroupBy();
        groupBy.groupName = "name"
        groupBy.groupFilterType = GlobalFilterType.EventProperties
        val aggregation = mongoQueryUtil.buildLiveUserAggregation(listOf("testuser"), groupBy, 5, ZoneId.of("Europe/Paris"))
        println(aggregation.toString())
    }

    @Test
    fun testLiveUserTrend1(){
        val aggregation = mongoQueryUtil.buildLiveUserTrendAggregation(listOf("testuser1", "testuser2"), listOf("2018-08-10", "2018-08-20"), 5, ZoneId.of("Europe/Paris"))
        println(aggregation.toString())
    }

    @Test
    //buildCountTrendAggregation(userIds: List<String>, requestFilter: EventReport.EventReportFilter, entityType: EventReport.EntityType, groupBy: GroupBy, tz: ZoneId, clientId: Long)
    fun testCountTrendAggregationWithBothFilterAndEventGroupBy(){
        val groupBy = GroupBy();
        groupBy.groupName = "browser"
        groupBy.groupFilterType = GlobalFilterType.Technographics

        val userGlobalFilter = GlobalFilter()
        userGlobalFilter.values = listOf("xyz")
        userGlobalFilter.operator = StringOperator.Contains.name
        userGlobalFilter.name = "lastname"
        userGlobalFilter.globalFilterType = GlobalFilterType.Demographics

        val eventReportFilter = EventReport.EventReportFilter(1001, "2018-08-10", "2018-08-20", "Search", listOf(userGlobalFilter))

        val aggregationForUserCount = mongoQueryUtil.buildCountTrendAggregation(listOf("testuser1", "testuser2"), eventReportFilter, EventReport.EntityType.user,
                groupBy, ZoneId.of("Europe/Paris"), 1001)
        println(aggregationForUserCount.toString())

        val aggregationForEventCount = mongoQueryUtil.buildCountTrendAggregation(listOf("testuser1", "testuser2"), eventReportFilter, EventReport.EntityType.event,
                groupBy, ZoneId.of("Europe/Paris"), 1001)
        println(aggregationForEventCount.toString())
    }

    @Test
    //buildCountTrendAggregation(userIds: List<String>, requestFilter: EventReport.EventReportFilter, entityType: EventReport.EntityType, groupBy: GroupBy, tz: ZoneId, clientId: Long)
    fun testCountTrendAggregationWithBothFilterAndUserGroupBy(){
        val groupBy = GroupBy();
        groupBy.groupName = "lastname"
        groupBy.groupFilterType = GlobalFilterType.Demographics

        val userGlobalFilter = GlobalFilter()
        userGlobalFilter.values = listOf("xyz")
        userGlobalFilter.operator = StringOperator.Contains.name
        userGlobalFilter.name = "lastname"
        userGlobalFilter.globalFilterType = GlobalFilterType.Demographics

        val eventReportFilter = EventReport.EventReportFilter(1001, "2018-08-10", "2018-08-20", "Search", listOf(userGlobalFilter))

        val aggregationForUserCount = mongoQueryUtil.buildCountTrendAggregation(listOf("testuser1", "testuser2"), eventReportFilter, EventReport.EntityType.user,
                groupBy, ZoneId.of("Europe/Paris"), 1001)
        println(aggregationForUserCount.toString())

        val aggregationForEventCount = mongoQueryUtil.buildCountTrendAggregation(listOf("testuser1", "testuser2"), eventReportFilter, EventReport.EntityType.event,
                groupBy, ZoneId.of("Europe/Paris"), 1001)
        println(aggregationForEventCount.toString())
    }

    @Test
    //buildCountTrendAggregation(userIds: List<String>, requestFilter: EventReport.EventReportFilter, entityType: EventReport.EntityType, groupBy: GroupBy, tz: ZoneId, clientId: Long)
    fun testCountTrendAggregatioWithNoFilterAndEventGroupBy(){
        val groupBy = GroupBy();
        groupBy.groupName = "browser"
        groupBy.groupFilterType = GlobalFilterType.Technographics

        val eventReportFilter = EventReport.EventReportFilter(1001, "2018-08-10", "2018-08-20", "Search")

        val aggregationForUserCount = mongoQueryUtil.buildCountTrendAggregation(listOf("testuser1", "testuser2"), eventReportFilter, EventReport.EntityType.user,
                groupBy, ZoneId.of("Europe/Paris"), 1001)
        println(aggregationForUserCount.toString())

        val aggregationForEventCount = mongoQueryUtil.buildCountTrendAggregation(listOf("testuser1", "testuser2"), eventReportFilter, EventReport.EntityType.event,
                groupBy, ZoneId.of("Europe/Paris"), 1001)
        println(aggregationForEventCount.toString())
    }

    @Test
    //buildCountTrendAggregation(userIds: List<String>, requestFilter: EventReport.EventReportFilter, entityType: EventReport.EntityType, groupBy: GroupBy, tz: ZoneId, clientId: Long)
    fun testCountTrendAggregatioWithNoFilterAndUserGroupBy(){
        val groupBy = GroupBy();
        groupBy.groupName = "lastname"
        groupBy.groupFilterType = GlobalFilterType.Demographics

        val eventReportFilter = EventReport.EventReportFilter(1001, "2018-08-10", "2018-08-20", "Search")

        val aggregationForUserCount = mongoQueryUtil.buildCountTrendAggregation(listOf("testuser1", "testuser2"), eventReportFilter, EventReport.EntityType.user,
                groupBy, ZoneId.of("Europe/Paris"), 1001)
        println(aggregationForUserCount.toString())

        val aggregationForEventCount = mongoQueryUtil.buildCountTrendAggregation(listOf("testuser1", "testuser2"), eventReportFilter, EventReport.EntityType.event,
                groupBy, ZoneId.of("Europe/Paris"), 1001)
        println(aggregationForEventCount.toString())
    }
}