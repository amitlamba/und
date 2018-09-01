package com.und.report.web.model

import java.time.LocalDate

class EventReport {

    data class PropertyFilter(var entityType: EntityType, var name: String, var value: String)
    data class EventReportFilter(var segmentid: Long, var fromDate: LocalDate, var toDate: LocalDate, var eventName: String, var propFilter: List<EventReport.PropertyFilter>)


    data class EventCount(var usercount: Int, var eventcount: Int, var name: String)
    data class EventPeriodCount(var usercount: Int, var eventcount: Int, var period: String)
    data class EventUserFrequency(var usercount: Int, var eventcount: Int)
    data class EventTimeFrequency(var eventCount: Int, var timeRange: String)

    data class Aggregate(var sum:Long, var period:String)

    enum class PERIOD {
        daily, weekly, monthly
    }

    enum class EntityType {
        event, user
    }
}