package com.und.report.web.model

import com.und.web.model.GlobalFilterType

class FunnelReport {
    data class FunnelReportFilter(
            var segmentid: Long,
            var days: Long,
            var steps: List<Step>,
            var funnelOrder: FunnelOrder,
            var conversionTime: Int,
            var splitProprty: String?,
            var splitProprtyType: GlobalFilterType = GlobalFilterType.EventAttributeProperties
    )


    enum class FunnelOrder {
        strict,
        default
    }

    data class Step(var order: Int, var eventName: String)

    data class FunnelStep(var step: Step, var count: Long, var property: String)

}