package com.und.report.service

import com.und.model.IncludeUsers
import com.und.report.web.model.*
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestParam

@Service
interface UserEventAnalyticsService {

    fun liveUsers(segmentId: Long, groupBy: GroupBy, interval: Long,includeUsers: IncludeUsers,clientId:Long?): List<UserCountForProperty>

    fun liveUserTrend(segmentId: Long, dates: List<String>, interval: Long,includeUsers: IncludeUsers,clientId: Long?): List<UserCountTrendForDate>

    fun liveUserByTypeTrend(segmentId: Long, date: List<String>, interval: Long,includeUsers: IncludeUsers,clientId: Long?):List<UserTypeTrendForDate>

    fun userCountByEvent(segmentId: Long, dates: List<String>,includeUsers: IncludeUsers,clientId: Long?):List<UserCountByEventForDate>

    fun eventReachability(requestFilter: EventReport.EventReportFilter, entityType: EventReport.EntityType, groupBy: GroupBy,includeUsers: IncludeUsers,clientId: Long?):Reachability

    fun countTrend(requestFilter: EventReport.EventReportFilter, entityType: EventReport.EntityType, groupBy: GroupBy,includeUsers: IncludeUsers,clientId: Long?): List<EventReport.EventCount>

    fun timePeriodTrend(requestFilter: EventReport.EventReportFilter, entityType: EventReport.EntityType, period: EventReport.PERIOD,includeUsers: IncludeUsers,clientId: Long?): List<EventReport.EventPeriodCount>

    fun eventUserTrend(requestFilter: EventReport.EventReportFilter,includeUsers: IncludeUsers,clientId: Long?): List<EventReport.EventUserFrequency>

    fun eventTimeTrend(requestFilter: EventReport.EventReportFilter,includeUsers: IncludeUsers,clientId: Long?): List<EventReport.EventTimeFrequency>

    fun aggregateTrend(requestFilter: EventReport.EventReportFilter, period: EventReport.PERIOD, aggregateBy: AggregateBy,includeUsers: IncludeUsers,clientId: Long?): List<EventReport.Aggregate>
}