package com.und.report.web.model

class FunnelReport {
    data class FunnelReportFilter(
            var segmentid: Long,
            var days: Long,
            var eventNames: List<String>,
            var funnelOrder: FunnelOrder,
            var conversionTime: Int,
            var splitProprty: String
    )


    enum class FunnelOrder {
        strict,
        default
    }

    data class FunnelStep(var step: String, var count: Long, var property: String)

}