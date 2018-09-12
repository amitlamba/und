package com.und.service

import com.und.report.web.model.AggregateBy
import com.und.report.web.model.EventReport
import com.und.report.web.model.GroupBy
import com.und.web.model.*
import com.und.web.model.Unit
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.test.util.ReflectionTestUtils
import java.time.ZoneId
import com.und.service.AggregationQuerybuilder.*

@RunWith(MockitoJUnitRunner::class)
class AggregationQuerybuilderTest {


    private lateinit var segmentParserCriteria: SegmentParserCriteria

    private lateinit var agregationQuerybuilder: AggregationQuerybuilder

    @Before
    fun setup(){
        segmentParserCriteria= SegmentParserCriteria()
        agregationQuerybuilder = AggregationQuerybuilder()
        ReflectionTestUtils.setField(agregationQuerybuilder, "segmentParserCriteria", segmentParserCriteria)
    }

    private fun getEventFilters(): List<GlobalFilter>{
        val filters = mutableListOf<GlobalFilter>()
        val eventNameFilter = buildFilter(GlobalFilterType.EventProperties, AggregationQuerybuilder.Field.EventName.fName, DataType.string, StringOperator.Equals.name,
                listOf("Search"), null)
        filters.add(eventNameFilter)

        val userIdFilter = buildFilter(GlobalFilterType.EventProperties, Field.UserId.fName, DataType.string, StringOperator.Contains.name, listOf("5b767f5bcfd0d1139b8659eb"), null)
        filters.add(userIdFilter)

        return filters;
    }

    private fun getUserFilters(): List<GlobalFilter> {
        return listOf(buildFilter(GlobalFilterType.Demographics, "lastname", DataType.string, StringOperator.Contains.name,
                listOf("Rivera"), null))
    }

    private fun getEventGroup1(): List<GroupBy>{
        val groupBy = GroupBy();
        groupBy.name = "os"
        groupBy.globalFilterType = GlobalFilterType.Technographics
        return listOf(groupBy)
    }

    private fun getEventGroup2(): List<GroupBy>{
        val groupBy = GroupBy();
        groupBy.name = "name"
        groupBy.globalFilterType = GlobalFilterType.EventProperties
        return listOf(groupBy)
    }

    private fun getUserGroup1(): List<GroupBy>{
        val groupBy = GroupBy();
        groupBy.name = "gender"
        groupBy.globalFilterType = GlobalFilterType.Demographics
        return listOf(groupBy)
    }

    @Test
    fun testCountTrendForEventFilterAndSimpleEventGroupBy(){
        val ten = "10.0"
        val tenInt = ten.toDouble().toInt()

        val groupBy = GroupBy();
        groupBy.name = "name"
        groupBy.globalFilterType = GlobalFilterType.EventProperties
        val groupBys =  listOf(groupBy)

        val filters = mutableListOf<GlobalFilter>()
        val userIdFilter = buildFilter(GlobalFilterType.EventProperties, Field.UserId.fName, DataType.string, StringOperator.Contains.name, listOf("5b767f5bcfd0d1139b8659eb"), null)
        filters.add(userIdFilter)

        val eventAggregation =  agregationQuerybuilder.buildAggregation(filters, groupBys, null, emptyMap (), EventReport.EntityType.event, ZoneId.of("Europe/Paris"), 3)
        println("testCountTrendForEventFilterAndEventGroupBy EventCount: $eventAggregation")

        val userAggregation =  agregationQuerybuilder.buildAggregation(filters, groupBys, null, emptyMap (), EventReport.EntityType.user, ZoneId.of("Europe/Paris"), 3)
        println("testCountTrendForEventFilterAndEventGroupBy UserCount: $userAggregation")
    }

    @Test
    fun testCountTrendForEventFilterAndEventGroupBy(){
        val groupBys = getEventGroup1()
        val filters = getEventFilters()

        val eventAggregation =  agregationQuerybuilder.buildAggregation(filters, groupBys, null, emptyMap (), EventReport.EntityType.event, ZoneId.of("Europe/Paris"), 3)
        println("testCountTrendForEventFilterAndEventGroupBy EventCount: $eventAggregation")

        val userAggregation =  agregationQuerybuilder.buildAggregation(filters, groupBys, null, emptyMap (), EventReport.EntityType.user, ZoneId.of("Europe/Paris"), 3)
        println("testCountTrendForEventFilterAndEventGroupBy UserCount: $userAggregation")
    }

    @Test
    fun testCountTrendForEventFilterAndUserGroupBy(){
        val groupBys = getUserGroup1()
        val filters = getEventFilters()

        val eventAggregation =  agregationQuerybuilder.buildAggregation(filters, groupBys, null, emptyMap (), EventReport.EntityType.event, ZoneId.of("Europe/Paris"), 3)
        println("testCountTrendForEventFilterAndUserGroupBy EventCount: $eventAggregation")

        val userAggregation =  agregationQuerybuilder.buildAggregation(filters, groupBys, null, emptyMap (), EventReport.EntityType.user, ZoneId.of("Europe/Paris"), 3)
        println("testCountTrendForEventFilterAndUserGroupBy UserCount: $userAggregation")
    }

    @Test
    fun testCountTrendForUserFilterAndEventGroupBy(){
        val groupBys = getEventGroup1()
        val filters = getUserFilters()

        val eventAggregation =  agregationQuerybuilder.buildAggregation(filters, groupBys, null, emptyMap (), EventReport.EntityType.event, ZoneId.of("Europe/Paris"), 3)
        println("testCountTrendForUserFilterAndEventGroupBy EventCount: $eventAggregation")

        val userAggregation =  agregationQuerybuilder.buildAggregation(filters, groupBys, null, emptyMap (), EventReport.EntityType.user, ZoneId.of("Europe/Paris"), 3)
        println("testCountTrendForUserFilterAndEventGroupBy UserCount: $userAggregation")
    }

    @Test
    fun testCountTrendForUserFilterAndUserGroupBy(){
        val groupBys = getUserGroup1()
        val filters = getUserFilters()

        val eventAggregation =  agregationQuerybuilder.buildAggregation(filters, groupBys, null, emptyMap (), EventReport.EntityType.event, ZoneId.of("Europe/Paris"), 3)
        println("testCountTrendForUserFilterAndUserGroupBy EventCount: $eventAggregation")

        val userAggregation =  agregationQuerybuilder.buildAggregation(filters, groupBys, null, emptyMap (), EventReport.EntityType.user, ZoneId.of("Europe/Paris"), 3)
        println("testCountTrendForUserFilterAndUserGroupBy UserCount: $userAggregation")
    }

    @Test
    fun testCountTrendForBothFilterAndEventGroupBy(){
        val groupBys = getEventGroup1()

        val filters = mutableListOf<GlobalFilter>()
        filters.addAll(getEventFilters())
        filters.addAll(getUserFilters())

        val eventAggregation =  agregationQuerybuilder.buildAggregation(filters, groupBys, null, emptyMap (), EventReport.EntityType.event, ZoneId.of("Europe/Paris"), 3)
        println("testCountTrendForBothFilterAndEventGroupBy EventCount: $eventAggregation")

        val userAggregation =  agregationQuerybuilder.buildAggregation(filters, groupBys, null, emptyMap (), EventReport.EntityType.user, ZoneId.of("Europe/Paris"), 3)
        println("testCountTrendForBothFilterAndEventGroupBy UserCount: $userAggregation")
    }


    @Test
    fun testCountTrendForBothFilterAndUserGroupBy(){
        val groupBys = getUserGroup1()

        val filters = mutableListOf<GlobalFilter>()
        filters.addAll(getEventFilters())
        filters.addAll(getUserFilters())

        val eventAggregation =  agregationQuerybuilder.buildAggregation(filters, groupBys, null, emptyMap (), EventReport.EntityType.event, ZoneId.of("Europe/Paris"), 3)
        println("testCountTrendForBothFilterAndUserGroupBy EventCount: $eventAggregation")

        val userAggregation =  agregationQuerybuilder.buildAggregation(filters, groupBys, null, emptyMap (), EventReport.EntityType.user, ZoneId.of("Europe/Paris"), 3)
        println("testCountTrendForBothFilterAndUserGroupBy UserCount: $userAggregation")
    }


    @Test
    fun testCountTrendByPeriodForBothFilterAndGroupByPeriod(){
        val monthGroupBy = GroupBy();
        monthGroupBy.name = "month"
        monthGroupBy.globalFilterType = GlobalFilterType.EventTimeProperties

        val yearGroupBy = GroupBy();
        yearGroupBy.name = "year"
        yearGroupBy.globalFilterType = GlobalFilterType.EventTimeProperties

        val groupBys = listOf(monthGroupBy, yearGroupBy)

        val filters = mutableListOf<GlobalFilter>()
        filters.addAll(getEventFilters())
        filters.addAll(getUserFilters())

        val eventAggregation =  agregationQuerybuilder.buildAggregation(filters, groupBys, null, emptyMap (), EventReport.EntityType.event, ZoneId.of("Europe/Paris"), 3)
        println("testCountTrendForBothFilterAndEventGroupBy EventCount: $eventAggregation")

        val userAggregation =  agregationQuerybuilder.buildAggregation(filters, groupBys, null, emptyMap (), EventReport.EntityType.user, ZoneId.of("Europe/Paris"), 3)
        println("testCountTrendForBothFilterAndEventGroupBy UserCount: $userAggregation")
    }

    @Test
    fun testCountTrendByPeriodForBothFilterAndGroupByPeriodAndAggregateByClientId(){
        val monthGroupBy = GroupBy()
        monthGroupBy.name = "month"
        monthGroupBy.globalFilterType = GlobalFilterType.EventTimeProperties

        val yearGroupBy = GroupBy()
        yearGroupBy.name = "year"
        yearGroupBy.globalFilterType = GlobalFilterType.EventTimeProperties

        val aggregateBy = AggregateBy()
        aggregateBy.aggregationType = AggregationType.Sum
        aggregateBy.name = "clientId"
        aggregateBy.globalFilterType = GlobalFilterType.EventProperties

        val groupBys = listOf(monthGroupBy, yearGroupBy)

        val filters = mutableListOf<GlobalFilter>()
        filters.addAll(getEventFilters())
        filters.addAll(getUserFilters())

        val eventAggregation =  agregationQuerybuilder.buildAggregation(filters, groupBys, aggregateBy, emptyMap (), EventReport.EntityType.event, ZoneId.of("Europe/Paris"), 3)
        println("testCountTrendForBothFilterAndEventGroupBy EventCount: $eventAggregation")

        val userAggregateBy = AggregateBy()
        userAggregateBy.aggregationType = AggregationType.Sum
        userAggregateBy.name = "countryCode"
        userAggregateBy.globalFilterType = GlobalFilterType.UserProperties

        val userAggregation =  agregationQuerybuilder.buildAggregation(filters, groupBys, userAggregateBy, emptyMap (), EventReport.EntityType.user, ZoneId.of("Europe/Paris"), 3)
        println("testCountTrendForBothFilterAndEventGroupBy UserCount: $userAggregation")
    }

    @Test
    fun testHourlyCountTrendForBothFilter(){
        val monthGroupBy = GroupBy()
        monthGroupBy.name = "hour"
        monthGroupBy.globalFilterType = GlobalFilterType.EventTimeProperties

        val yearGroupBy = GroupBy()
        yearGroupBy.name = "year"
        yearGroupBy.globalFilterType = GlobalFilterType.EventTimeProperties

        val aggregateBy = AggregateBy()
        aggregateBy.aggregationType = AggregationType.Sum
        aggregateBy.name = "clientId"
        aggregateBy.globalFilterType = GlobalFilterType.EventProperties

        val groupBys = listOf(monthGroupBy)

        val filters = mutableListOf<GlobalFilter>()
        filters.addAll(getEventFilters())
        filters.addAll(getUserFilters())

        val eventAggregation =  agregationQuerybuilder.buildAggregation(filters, groupBys, null, emptyMap (), EventReport.EntityType.event, ZoneId.of("Europe/Paris"), 3)
        println("testCountTrendForBothFilterAndEventGroupBy EventCount: $eventAggregation")

        val userAggregation =  agregationQuerybuilder.buildAggregation(filters, groupBys, null, emptyMap (), EventReport.EntityType.user, ZoneId.of("Europe/Paris"), 3)
        println("testCountTrendForBothFilterAndEventGroupBy UserCount: $userAggregation")
    }




    @Test
    fun testUserCountByEventForEventFilterAndEventGroupBy(){
        val groupBy1 = GroupBy();
        groupBy1.name = "dateVal"
        groupBy1.globalFilterType = GlobalFilterType.EventComputedProperties

        val groupBy2 = GroupBy();
        groupBy2.name = "userId"
        groupBy2.globalFilterType = GlobalFilterType.EventProperties
        val groupBys = listOf(groupBy1, groupBy2)


        val filters = mutableListOf<GlobalFilter>()
        val userIdFilter = buildFilter(GlobalFilterType.EventProperties, Field.UserId.fName, DataType.string, StringOperator.Contains.name,
                listOf("5b767f5bcfd0d1139b8659eb", "5b767f5ccfd0d1139b8659ed", "5b767f5dcfd0d1139b8659f1"), null)
        filters.add(userIdFilter)

        val dateFilter = buildFilter(GlobalFilterType.EventComputedProperties, Field.DateVal.fName, DataType.string, StringOperator.Contains.name,
                listOf("2018-08-19", "2018-08-20", "2018-08-21"), null)
        filters.add(dateFilter)


        val eventAggregation =  agregationQuerybuilder.buildAggregation(filters, groupBys, null, emptyMap (), EventReport.EntityType.event, ZoneId.of("Europe/Paris"), 3)
        println("testCountTrendForEventFilterAndEventGroupBy EventCount: $eventAggregation")
    }

    @Test
    @Ignore
    fun testCountTrendForBothFilterAndBothGroupBy(){
        val groupBys = mutableListOf<GroupBy>()
        groupBys.addAll(getUserGroup1())
        groupBys.addAll(getEventGroup1())

        val eventNameFilter = buildFilter(GlobalFilterType.EventProperties, Field.EventName.fName, DataType.string, StringOperator.Equals.name,
                listOf("Search"), null)

        val filters = mutableListOf<GlobalFilter>()
        filters.add(eventNameFilter)
        filters.addAll(getUserFilters())

        val eventAggregation =  agregationQuerybuilder.buildAggregation(filters, groupBys, null, emptyMap (), EventReport.EntityType.event, ZoneId.of("Europe/Paris"), 3)
        println("testCountTrendForBothFilterAndBothGroupBy EventCount: $eventAggregation")

        val userAggregation =  agregationQuerybuilder.buildAggregation(filters, groupBys, null, emptyMap (), EventReport.EntityType.user, ZoneId.of("Europe/Paris"), 3)
        println("testCountTrendForBothFilterAndBothGroupBy UserCount: $userAggregation")
    }

    private fun buildFilter(globalFilterType: GlobalFilterType, name: String, type: DataType, operator: String, values: List<String>, valueUnit: Unit?): GlobalFilter{
        var filter = GlobalFilter()
        if(globalFilterType != null) filter.globalFilterType = globalFilterType
        if (name != null) filter.name = name
        if (type != null) filter.type = type
        if (operator != null) filter.operator = operator
        if (values != null) filter.values = values
        if (valueUnit != null) filter.valueUnit = valueUnit
        return filter
    }
}