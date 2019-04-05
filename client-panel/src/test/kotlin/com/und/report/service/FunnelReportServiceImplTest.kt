package com.und.report.service

import com.und.report.web.model.FunnelReport
import com.und.service.SegmentParserCriteria
import com.und.web.model.GlobalFilterType
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.test.util.ReflectionTestUtils
import java.time.ZoneId

@RunWith(MockitoJUnitRunner::class)
class FunnelReportServiceImplTest {

    private lateinit var segmentParserCriteria: SegmentParserCriteria

    private lateinit var funnelReportService: FunnelReportServiceImpl

    @Before
    fun setup(){
        segmentParserCriteria= SegmentParserCriteria()
        funnelReportService = FunnelReportServiceImpl()
        ReflectionTestUtils.setField(funnelReportService, "segmentParserCriteria", segmentParserCriteria)
    }

    @Test
    fun testWithoutSplit(){

        val funnelFilter = FunnelReport.FunnelReportFilter(segmentid = 1, conversionTime = 10, days = 10,
                funnelOrder = FunnelReport.FunnelOrder.default, splitProperty = null, splitPropertyType = GlobalFilterType.Technographics,
                steps = listOf("Search", "View").map { it -> FunnelReport.Step(eventName = it, order = 1) })

        val eventAggregation =  funnelReportService.buildAggregation(funnelFilter, 3, listOf("5b767f5bcfd0d1139b8659eb", "5b767f5ccfd0d1139b8659ed", "5b767f5dcfd0d1139b8659f1"), ZoneId.of("Europe/Paris"),true)

        println("test eventAggregation: $eventAggregation")
    }

    @Test
    fun testWithSplit(){

        val funnelFilter = FunnelReport.FunnelReportFilter(segmentid = 1, conversionTime = 10, days = 10,
                funnelOrder = FunnelReport.FunnelOrder.default, splitProperty = "os", splitPropertyType = GlobalFilterType.Technographics,
                steps = listOf("Search", "View").map { it -> FunnelReport.Step(eventName = it, order = 1) })

        val eventAggregation =  funnelReportService.buildAggregation(funnelFilter, 3, listOf("5b767f5bcfd0d1139b8659eb", "5b767f5ccfd0d1139b8659ed", "5b767f5dcfd0d1139b8659f1"), ZoneId.of("Europe/Paris"),true)

        println("test eventAggregation: $eventAggregation")
    }
}