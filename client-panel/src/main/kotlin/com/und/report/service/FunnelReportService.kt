package com.und.report.service

import com.und.model.IncludeUsers
import com.und.report.web.model.FunnelReport

interface FunnelReportService {

    fun funnel(funnelFilter: FunnelReport.FunnelReportFilter,includeUsers: IncludeUsers): List<FunnelReport.FunnelStep>
}