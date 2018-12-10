package com.und.report.service

import com.und.report.web.model.*
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.RequestParam

@Service
interface UserEventAnalyticsService {

    fun liveUsers(segmentId: Long, groupBy: GroupBy, interval: Long): List<UserCountForProperty>

    fun liveUserTrend(segmentId: Long, dates: List<String>, interval: Long): List<UserCountTrendForDate>

    fun liveUserByTypeTrend(segmentId: Long, date: List<String>, interval: Long):List<UserTypeTrendForDate>

    fun userCountByEvent(segmentId: Long, dates: List<String>):List<UserCountByEventForDate>

    fun eventReachability(requestFilter: EventReport.EventReportFilter, entityType: EventReport.EntityType, groupBy: GroupBy):Reachability

    fun countTrend(requestFilter: EventReport.EventReportFilter, entityType: EventReport.EntityType, groupBy: GroupBy): List<EventReport.EventCount>

    fun timePeriodTrend(requestFilter: EventReport.EventReportFilter, entityType: EventReport.EntityType, period: EventReport.PERIOD): List<EventReport.EventPeriodCount>

    fun eventUserTrend(requestFilter: EventReport.EventReportFilter): List<EventReport.EventUserFrequency>

    fun eventTimeTrend(requestFilter: EventReport.EventReportFilter): List<EventReport.EventTimeFrequency>

    fun aggregateTrend(requestFilter: EventReport.EventReportFilter, period: EventReport.PERIOD, aggregateBy: AggregateBy): List<EventReport.Aggregate>
}