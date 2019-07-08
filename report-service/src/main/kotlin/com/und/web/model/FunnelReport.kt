package com.und.report.web.model

import com.und.web.model.GlobalFilter
import com.und.web.model.GlobalFilterType

class FunnelReport {
    data class FunnelReportFilter(
            var segmentid: Long,
            var days: Long,
            var steps: List<FunnelReport.Step> = emptyList(),
            var funnelOrder: FunnelOrder,
            var conversionTime: Int,
            var filters: List<GlobalFilter> = arrayListOf(),
            var splitProperty: String?,
            var splitPropertyType: GlobalFilterType = GlobalFilterType.EventAttributeProperties
    )


    enum class FunnelOrder {
        strict,
        default
    }

    data class Step(var order: Int, var eventName: String)

    data class FunnelStep(var step: Step, var count: Long, var property: String)

}

class FunnelStepAndFilter{
    var steps: List<FunnelReport.Step> = emptyList()
    var filters: List<GlobalFilter> = arrayListOf()
}