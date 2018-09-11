package com.und.report.web.model

import com.und.web.model.GlobalFilter
import com.und.web.model.GlobalFilterType
import java.io.Serializable
import java.time.LocalDate

class EventReport {

    data class PropertyFilter(var entityType: EntityType, var name: String, var value: String)

    data class EventReportFilter(var segmentid: Long, var fromDate: String, var toDate: String, var eventName: String, var propFilter: List<GlobalFilter> = emptyList())


    data class EventCount(var count: Int, var groupedBy: Map<String, Any>)
    data class EventPeriodCount(var count: Int, var period: Map<String, Any>)

    data class EventUserFrequency(var usercount: Int, var eventcount: Int)
    data class EventTimeFrequency(var eventCount: Int, var hour: Int)

    data class Aggregate(var sum:Long, var period: Map<String, Any>)

    enum class PERIOD {
        daily, weekly, monthly
    }

    enum class EntityType {
        event, user
    }
}
