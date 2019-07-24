package com.und.service

import com.und.model.AggregateOutput
import com.und.model.IncludeUsers
import com.und.repository.mongo.UserAnalyticsRepository
import com.und.web.model.*
import com.und.service.*
import com.und.web.model.*
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.aggregation.Aggregation
import com.und.service.AggregationQuerybuilder.*
import com.und.util.AGGREGATE_VALUE
import com.und.util.NUM_OF_MINUTES
import com.und.util.loggerFor
import org.springframework.stereotype.Service

@Service
class UserEventAnalyticsServiceImpl : UserEventAnalyticsService {

    companion object {
        val logger: Logger = loggerFor(UserEventAnalyticsServiceImpl::class.java)
        const val allUser = ReportUtil.ALL_USER_SEGMENT
    }

    @Autowired
    private lateinit var userAnalyticsRepository: UserAnalyticsRepository

    @Autowired
    private lateinit var segmentService: SegmentService

    @Autowired
    private lateinit var userSettingsService: UserSettingsService

    @Autowired
    private lateinit var aggregationQuerybuilder: AggregationQuerybuilder

    override fun liveUsers(segmentId: Long, groupBy: GroupBy, interval: Long,includeUsers: IncludeUsers,clientID: Long?): List<UserCountForProperty> {
        logger.debug("Liveusers aggregation for segmentId : $segmentId, groupBy: $groupBy, interval: $interval")

        val clientID = clientID ?: return emptyList()
        val filters = createFilterList(segmentId, clientID, emptyList(),includeUsers)
        val userIdentified = when(includeUsers){
            IncludeUsers.KNOWN -> true
            IncludeUsers.UNKNOWN -> false
            IncludeUsers.ALL -> null
        }
        val groupBys = listOf(groupBy)
        //TODO include creationTime filter based on interval
        val tz = userSettingsService.getTimeZoneByClientId(clientID)
        val userAggregation = aggregationQuerybuilder.buildAggregation(filters, groupBys, null, emptyMap(), EventReport.EntityType.user, tz, clientID,userIdentified=userIdentified)

        val aggregateResult = userAnalyticsRepository.aggregate(userAggregation, clientID)
        return buildLiveUserResult(aggregateResult)
    }

    override fun liveUserTrend(segmentId: Long, dates: List<String>, interval: Long,includeUsers: IncludeUsers,clientId:Long?): List<UserCountTrendForDate> {
        logger.debug("Liveusers aggregation for segmentId : $segmentId, dates: $dates, interval: $interval")
        val clientID = clientId ?: return emptyList()
        val userIdentified = when(includeUsers){
            IncludeUsers.KNOWN -> true
            IncludeUsers.UNKNOWN -> false
            IncludeUsers.ALL -> null
        }
        val otherFilters = listOf(ReportUtil.buildFilter(GlobalFilterType.EventComputedProperties, Field.DateVal.fName, DataType.string, StringOperator.Contains.name, dates, null))
        val filters = createFilterList(segmentId, clientID, otherFilters,includeUsers)
        val groupBys = listOf(buildGroupBy(Field.DateVal.fName, GlobalFilterType.EventComputedProperties),
                buildGroupBy(Field.MinutesPeriod.fName, GlobalFilterType.EventComputedProperties))

        val propertyValues = mapOf<String, Any>(NUM_OF_MINUTES to interval.toString())

        val tz = userSettingsService.getTimeZoneByClientId(clientID)
        val userAggregation = aggregationQuerybuilder.buildAggregation(filters, groupBys, null, propertyValues, EventReport.EntityType.user, tz, clientID,userIdentified=userIdentified)
        val aggregateResult = userAnalyticsRepository.aggregate(userAggregation, clientID)
        return buildLiveUserTrendResult(aggregateResult)
    }

    override fun liveUserByTypeTrend(segmentId: Long, dates: List<String>, interval: Long,includeUsers: IncludeUsers,clientId:Long?): List<UserTypeTrendForDate> {
        logger.debug("LiveUSerByTypeTrend aggregation for segmentId : $segmentId, dates: $dates, interval: $interval")
        val clientID = clientId ?: return emptyList()
        val userIdentified = when(includeUsers){
            IncludeUsers.KNOWN -> true
            IncludeUsers.UNKNOWN -> false
            IncludeUsers.ALL -> null
        }
        val otherFilters = listOf(ReportUtil.buildFilter(GlobalFilterType.EventComputedProperties, Field.DateVal.fName, DataType.string, StringOperator.Contains.name, dates, null))
        val filters = createFilterList(segmentId, clientID, otherFilters,includeUsers)

        val groupBys = listOf(buildGroupBy(Field.DateVal.fName, GlobalFilterType.EventComputedProperties),
                buildGroupBy(Field.MinutesPeriod.fName, GlobalFilterType.EventComputedProperties),
                buildGroupBy("gender", GlobalFilterType.Demographics)) //last group by is a dummy group by to get similar query, it is replaced with userType group by later in this method

        val propertyValues = mapOf<String, Any>(NUM_OF_MINUTES to interval.toString())

        val tz = userSettingsService.getTimeZoneByClientId(clientID)
        val buildAggregationPipeline = aggregationQuerybuilder.buildAggregationPipeline(filters, groupBys, null, propertyValues, EventReport.EntityType.user, tz, clientID,userIdentified = userIdentified)
        val projectionOperation = Aggregation.project().and(Field.DateVal.fName).`as`(Field.DateVal.fName)
                .and(Field.MinutesPeriod.fName).`as`(Field.MinutesPeriod.fName)
                .and(aggregationQuerybuilder.getAggregationExpression(Field.UserType.fName, propertyValues)).`as`(Field.UserType.fName)
        val groupOperation = Aggregation.group(Field.DateVal.fName, Field.MinutesPeriod.fName, Field.UserType.fName).count().`as`(AGGREGATE_VALUE)

        val userAggregation = Aggregation.newAggregation(*buildAggregationPipeline.dropLast(2).toTypedArray(), projectionOperation, groupOperation)
        val aggregateResult = userAnalyticsRepository.aggregate(userAggregation, clientID)

        return buildLiveUserByTypeTrendResult(aggregateResult)
    }

    override fun userCountByEvent(segmentId: Long, dates: List<String>,includeUsers: IncludeUsers,clientId:Long?): List<UserCountByEventForDate> {
        logger.debug("Liveusers aggregation for segmentId : $segmentId, dates: $dates")
        val clientID = clientId ?: return emptyList()
        val userIdentified = when(includeUsers){
            IncludeUsers.KNOWN -> true
            IncludeUsers.UNKNOWN -> false
            IncludeUsers.ALL -> null
        }
        val otherFilters = listOf(ReportUtil.buildFilter(GlobalFilterType.EventComputedProperties, Field.DateVal.fName, DataType.string, StringOperator.Contains.name, dates, null))
        val filters = createFilterList(segmentId, clientID, otherFilters,includeUsers)

        val groupBys = listOf(buildGroupBy(Field.DateVal.fName, GlobalFilterType.EventComputedProperties),
                buildGroupBy(Field.EventName.fName, GlobalFilterType.EventProperties))

        val tz = userSettingsService.getTimeZoneByClientId(clientID)
        val userAggregation = aggregationQuerybuilder.buildAggregation(filters, groupBys, null, emptyMap(), EventReport.EntityType.user, tz, clientID,userIdentified = userIdentified)
        val aggregateResult = userAnalyticsRepository.aggregate(userAggregation, clientID)

        return buildUserCountByEventResult(aggregateResult)
    }

    override fun countTrend(requestFilter: EventReport.EventReportFilter, entityType: EventReport.EntityType, groupBy: GroupBy,includeUsers: IncludeUsers,clientId:Long?): List<EventReport.EventCount> {
        logger.debug("CountTrend aggregation for requestFilter : $requestFilter, entityType: $entityType, groupBy: $groupBy")
        val clientID = clientId ?: return emptyList()
        val userIdentified = when(includeUsers){
            IncludeUsers.KNOWN -> true
            IncludeUsers.UNKNOWN -> false
            IncludeUsers.ALL -> null
        }
        val tz = userSettingsService.getTimeZoneByClientId(clientID)
        val filters = buildCommonfilters(requestFilter, entityType, clientID,includeUsers)
        val aggregation = aggregationQuerybuilder.buildAggregation(filters, listOf(groupBy), null, emptyMap(), entityType, tz, clientID,userIdentified = userIdentified)

        val resultList = userAnalyticsRepository.aggregate(aggregation, clientID)

        return buildCountTrend(resultList)
    }

    override fun eventReachability(requestFilter: EventReport.EventReportFilter, entityType: EventReport.EntityType, groupBy: GroupBy,includeUsers: IncludeUsers,clientId: Long?): Reachability {
        logger.debug("EventReachability aggregation for requestFilter : $requestFilter, entityType: $entityType, groupBy: $groupBy")
        val clientID = clientId ?: return Reachability()
        val userIdentified = when(includeUsers){
            IncludeUsers.KNOWN -> true
            IncludeUsers.UNKNOWN -> false
            IncludeUsers.ALL -> null
        }
        val tz = userSettingsService.getTimeZoneByClientId(clientID)
        val filters = buildCommonfilters(requestFilter, entityType, clientID,includeUsers)
        val aggregation = aggregationQuerybuilder.buildAggregation(filters, listOf(groupBy), null, emptyMap(), entityType, tz, clientID, true,userIdentified = userIdentified)
        return userAnalyticsRepository.getReachability(aggregation,clientID)
    }

    override fun timePeriodTrend(requestFilter: EventReport.EventReportFilter, entityType: EventReport.EntityType, period: EventReport.PERIOD,includeUsers: IncludeUsers,clientId:Long?): List<EventReport.EventPeriodCount> {
        logger.debug("TimePeriodTrend aggregation for requestFilter : $requestFilter, entityType: $entityType, period: $period")
        val clientID = clientId ?: return emptyList()
        val userIdentified = when(includeUsers){
            IncludeUsers.KNOWN -> true
            IncludeUsers.UNKNOWN -> false
            IncludeUsers.ALL -> null
        }
        val tz = userSettingsService.getTimeZoneByClientId(clientID)
        val filters = buildCommonfilters(requestFilter, entityType, clientID,includeUsers)
        val groupBys = buildTimePeriodGroupBy(period)
        val aggregation = aggregationQuerybuilder.buildAggregation(filters, groupBys, null, emptyMap(), entityType, tz, clientID,userIdentified = userIdentified)
        val resultList = userAnalyticsRepository.aggregate(aggregation, clientID)
        return buildTimePeriodTrend(resultList)
    }

//    private fun sortResult(resultList:List<AggregateOutput>):List<AggregateOutput>{
//        resultList.sortedBy { it -> it.groupByInfo.get("") }
//    }

    override fun eventUserTrend(requestFilter: EventReport.EventReportFilter,includeUsers: IncludeUsers,clientId:Long?): List<EventReport.EventUserFrequency> {
        logger.debug("EventUserTrend aggregation for requestFilter : $requestFilter")
        val clientID = clientId ?: return emptyList()
        val userIdentified = when(includeUsers){
            IncludeUsers.KNOWN -> true
            IncludeUsers.UNKNOWN -> false
            IncludeUsers.ALL -> null
        }
        val tz = userSettingsService.getTimeZoneByClientId(clientID)
        val filters = buildCommonfilters(requestFilter, EventReport.EntityType.event, clientID,includeUsers)
        val groupBys = listOf(buildGroupBy(Field.UserId.fName, GlobalFilterType.EventProperties))
        val aggregationPipeline = aggregationQuerybuilder.buildAggregationPipeline(filters, groupBys, null, emptyMap(), EventReport.EntityType.event, tz, clientID,userIdentified = userIdentified)

        val lastGroupOperation = Aggregation.group(AGGREGATE_VALUE).count().`as`(AGGREGATE_VALUE)
        val aggregation = Aggregation.newAggregation(*aggregationPipeline.toTypedArray(), lastGroupOperation)
        val resultList = userAnalyticsRepository.aggregate(aggregation, clientID)

        return buildEventUserTrend(resultList)
    }

    override fun eventTimeTrend(requestFilter: EventReport.EventReportFilter,includeUsers: IncludeUsers,clientId:Long?): List<EventReport.EventTimeFrequency> {
        logger.debug("EventTimeTrend aggregation for requestFilter : $requestFilter")
        val clientID = clientId ?: return emptyList()
        val userIdentified = when(includeUsers){
            IncludeUsers.KNOWN -> true
            IncludeUsers.UNKNOWN -> false
            IncludeUsers.ALL -> null
        }
        val tz = userSettingsService.getTimeZoneByClientId(clientID)
        val filters = buildCommonfilters(requestFilter, EventReport.EntityType.event, clientID,includeUsers)
        val groupBys = listOf(buildGroupBy(Field.Hour.fName, GlobalFilterType.EventTimeProperties))
        val aggregation = aggregationQuerybuilder.buildAggregation(filters, groupBys, null, emptyMap(), EventReport.EntityType.event, tz, clientID,userIdentified = userIdentified)
        val resultList = userAnalyticsRepository.aggregate(aggregation, clientID)

        return buildEventTimeTrend(resultList)
    }

    override fun aggregateTrend(requestFilter: EventReport.EventReportFilter, period: EventReport.PERIOD, aggregateBy: AggregateBy,includeUsers: IncludeUsers,clientId:Long?): List<EventReport.Aggregate> {
        logger.debug("AggregateTrend aggregation for requestFilter : $requestFilter, period: $period, aggregateBy: $aggregateBy")
        val clientID = clientId ?: return emptyList()
        val userIdentified = when(includeUsers){
            IncludeUsers.KNOWN -> true
            IncludeUsers.UNKNOWN -> false
            IncludeUsers.ALL -> null
        }
        val tz = userSettingsService.getTimeZoneByClientId(clientID)
        val filters = buildCommonfilters(requestFilter, EventReport.EntityType.event, clientID,includeUsers)
        val groupBys = buildTimePeriodGroupBy(period)
        val aggregation = aggregationQuerybuilder.buildAggregation(filters, groupBys, aggregateBy, emptyMap(), EventReport.EntityType.event, tz, clientID,userIdentified = userIdentified)
        val resultList = userAnalyticsRepository.aggregate(aggregation, clientID)

        return buildAggregateTrend(resultList)
    }

    private fun buildTimePeriodGroupBy(period: EventReport.PERIOD): List<GroupBy> {
        when (period) {
            EventReport.PERIOD.daily -> {
                return listOf(buildGroupBy("year", GlobalFilterType.EventTimeProperties), buildGroupBy("month", GlobalFilterType.EventTimeProperties), buildGroupBy("dayOfMonth", GlobalFilterType.EventTimeProperties))
            }
            EventReport.PERIOD.weekly -> {
                return listOf(buildGroupBy("year", GlobalFilterType.EventTimeProperties), buildGroupBy("month", GlobalFilterType.EventTimeProperties), buildGroupBy("dayOfMonth", GlobalFilterType.EventTimeProperties))
            }
            EventReport.PERIOD.monthly -> {
                return listOf(buildGroupBy("year", GlobalFilterType.EventTimeProperties), buildGroupBy("month", GlobalFilterType.EventTimeProperties))
            }
        }
    }

    private fun buildGroupBy(name: String, globalFilterType: GlobalFilterType): GroupBy {
        val groupBy = GroupBy()
        groupBy.groupName = name
        groupBy.groupFilterType = globalFilterType
        return groupBy
    }


    private fun buildCommonfilters(requestFilter: EventReport.EventReportFilter, entityType: EventReport.EntityType, clientID: Long,includeUsers: IncludeUsers): List<GlobalFilter> {

        val commonFilters = mutableListOf<GlobalFilter>()

        commonFilters.add(ReportUtil.buildFilter(GlobalFilterType.EventProperties, Field.EventName.fName, DataType.string, StringOperator.Equals.name,
                listOf(requestFilter.eventName), null))

        val otherFilters = listOf(ReportUtil.buildFilter(GlobalFilterType.EventProperties, Field.CreationTime.fName, DataType.date, DateOperator.Between.name,
                listOf(requestFilter.fromDate, requestFilter.toDate), null))
        val filters = createFilterList(requestFilter.segmentid, clientID, otherFilters,includeUsers)

        commonFilters.addAll(filters)
        commonFilters.addAll(requestFilter.propFilter)

        return commonFilters
    }


    private fun buildLiveUserResult(aggregate: List<AggregateOutput>): List<UserCountForProperty> {
        return aggregate.map { UserCountForProperty(it.aggregateVal.toInt(), it.groupByInfo) }
    }

    /**
     * Converting list of items like [{"_id" : {"dateVal" : "2018-08-20", "timePeriod" : 88.0}, "uniqueUserCount" : "4"}, {"_id" : {"dateVal" : "2018-08-20", "timePeriod" : 92.0}, "uniqueUserCount" : "6"}]
     * into list of {"dateVal" : "2018-08-20", [{"timePeriod" : 88.0, "uniqueUserCount" : "4"}, {"timePeriod" : 92.0, "uniqueUserCount" : "6"}]}
     */
    private fun buildLiveUserTrendResult(aggregate: List<AggregateOutput>): List<UserCountTrendForDate> {
        return aggregate.map {
            UserCountTrendForDate(it.groupByInfo[Field.DateVal.fName].toString(),
                    listOf(UserCountForTime(it.aggregateVal.toInt(), it.groupByInfo[Field.MinutesPeriod.fName].toString().toDouble().toInt())))
        }
                .groupBy({ it.date }, { it.trenddata.first() })
                .map { UserCountTrendForDate(it.key, it.value) }
    }

    private fun buildLiveUserByTypeTrendResult(aggregate: List<AggregateOutput>): List<UserTypeTrendForDate> {
        val groupedByDate = aggregate.map {
            mapOf(it.groupByInfo[Field.DateVal.fName] to mapOf(AGGREGATE_VALUE to it.aggregateVal.toInt(),
                    Field.MinutesPeriod.fName to it.groupByInfo[Field.MinutesPeriod.fName], Field.UserType.fName to it.groupByInfo[Field.UserType.fName]))
        }
                .groupBy({ it.keys.toList().first() }, { it.values.toList().first() })

        val result = mutableListOf<UserTypeTrendForDate>()
        groupedByDate.forEach {
            val userCountDataList = mutableListOf<UserCountByTypeForTime>()
            val groupedByTime = it.value.groupBy({ it[Field.MinutesPeriod.fName].toString().toDouble().toInt() }, { it })

            groupedByTime.forEach {
                val newUserCountList = it.value.filter { it[Field.UserType.fName] == "new" }
                val oldUserCountList = it.value.filter { it[Field.UserType.fName] == "old" }

                val newUserCount = if (newUserCountList.isEmpty()) 0 else newUserCountList.first()[AGGREGATE_VALUE].toString().toDouble().toInt()
                val oldUserCount = if (oldUserCountList.isEmpty()) 0 else oldUserCountList.first()[AGGREGATE_VALUE].toString().toDouble().toInt()
                val record = UserCountByTypeForTime(newUserCount, oldUserCount, it.key)
                userCountDataList.add(record)
            }
            result.add(UserTypeTrendForDate(it.key.toString(), userCountDataList))
        }

        return result
    }


    private fun  buildUserCountByEventResult(aggregate: List<AggregateOutput>): List<UserCountByEventForDate> {
        return aggregate.map {
            UserCountByEventForDate(it.groupByInfo[Field.DateVal.fName].toString(),
                    listOf(UserCountByEvent(it.aggregateVal.toInt(), it.groupByInfo[Field.EventName.fName].toString())))
        }
                .groupBy({ it.date }, { it.userCountData.first() })
                .map { UserCountByEventForDate(it.key, it.value) }
    }

    private fun buildCountTrend(aggregate: List<AggregateOutput>): List<EventReport.EventCount> {
        return aggregate.map { EventReport.EventCount(it.aggregateVal.toInt(), it.groupByInfo) }
    }

    private fun buildTimePeriodTrend(aggregate: List<AggregateOutput>): List<EventReport.EventPeriodCount> {
        return aggregate.map { EventReport.EventPeriodCount(it.aggregateVal.toInt(), it.groupByInfo) }
    }

    private fun buildEventUserTrend(aggregate: List<AggregateOutput>): List<EventReport.EventUserFrequency> {
        return aggregate.map { EventReport.EventUserFrequency(it.aggregateVal.toInt(), it.groupByInfo["_id"].toString().toDouble().toInt()) }
    }

    private fun buildEventTimeTrend(aggregate: List<AggregateOutput>): List<EventReport.EventTimeFrequency> {
        return aggregate.map { EventReport.EventTimeFrequency(it.aggregateVal.toInt(), it.groupByInfo["_id"].toString().toDouble().toInt()) }
    }

    private fun buildAggregateTrend(aggregate: List<AggregateOutput>): List<EventReport.Aggregate> {
        return aggregate.map { EventReport.Aggregate(it.aggregateVal.toLong(), it.groupByInfo) }
    }

    private fun createFilterList(segmentId: Long, clientID: Long, otherFilters: List<GlobalFilter>,includeUsers: IncludeUsers): List<GlobalFilter> {
        val filters = mutableListOf<GlobalFilter>()
        if (segmentId != allUser) {
            val segmentUserIds = segmentService.segmentUserIds(segmentId, clientID,includeUsers)
            val segmentFilter = ReportUtil.buildFilter(GlobalFilterType.EventProperties, Field.UserId.fName, DataType.string, StringOperator.Contains.name, segmentUserIds, null)
            filters.add(segmentFilter)

        }
        if (otherFilters.isNotEmpty())
            filters.addAll(otherFilters)
        return filters.toList()

    }


}