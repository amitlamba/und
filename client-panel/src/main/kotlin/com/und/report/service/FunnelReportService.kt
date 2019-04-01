package com.und.report.service

import com.und.report.web.model.FunnelReport

interface FunnelReportService {

    fun funnel(funnelFilter: FunnelReport.FunnelReportFilter): List<FunnelReport.FunnelStep>
    fun getWinnerTemplate(clientId:Long,campaignId:Long):Long
}