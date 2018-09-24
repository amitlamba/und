package com.und.report.service

import com.und.common.utils.loggerFor
import com.und.report.repository.mongo.UserAnalyticsRepository
import com.und.report.web.model.FunnelReport
import com.und.service.SegmentParserCriteria
import com.und.service.SegmentService
import com.und.service.UserSettingsService
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.stereotype.Component


const val FUNNEL_QUERY_PAGE_SIZE = 100

@Component
class FunnelReportServiceImpl: FunnelReportService {

    companion object {
        val logger: Logger = loggerFor(FunnelReportServiceImpl::class.java)
    }

    @Autowired
    private lateinit var segmentParserCriteria: SegmentParserCriteria

    @Autowired
    private lateinit var userAnalyticsRepository: UserAnalyticsRepository

    @Autowired
    private lateinit var segmentService: SegmentService

    @Autowired
    private lateinit var userSettingsService: UserSettingsService


    override fun funnel(funnelFilter: FunnelReport.FunnelReportFilter): List<FunnelReport.FunnelStep> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun buildAggregation(funnelFilter: FunnelReport.FunnelReportFilter): Aggregation {





        TODO("not implemented")
    }
}