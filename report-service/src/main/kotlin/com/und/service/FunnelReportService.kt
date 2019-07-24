package com.und.service

import com.und.model.IncludeUsers
import com.und.web.model.FunnelReport

interface FunnelReportService {


//    fun funnel(funnelFilter: FunnelReport.FunnelReportFilter): List<FunnelReport.FunnelStep>
    fun getWinnerTemplate(clientId:Long,campaignId:Long,includeUsers: IncludeUsers):Long

    fun funnel(funnelFilter: FunnelReport.FunnelReportFilter,includeUsers: IncludeUsers,clientId: Long?=null): List<FunnelReport.FunnelStep>

}