package com.und.report.service

import com.mongodb.BasicDBObject
import com.und.common.utils.loggerFor
import com.und.report.model.FunnelData
import com.und.report.repository.mongo.UserAnalyticsRepository
import com.und.report.web.model.FunnelReport
import com.und.security.utils.AuthenticationUtils
import com.und.service.AggregationQuerybuilder
import com.und.service.SegmentParserCriteria
import com.und.service.SegmentService
import com.und.service.UserSettingsService
import com.und.web.model.DataType
import com.und.web.model.GlobalFilterType
import com.und.web.model.StringOperator
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.aggregation.*
import org.springframework.stereotype.Component
import java.time.ZoneId


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

    @Autowired
    private lateinit var awsFunnelLambdaInvoker: AWSFunnelLambdaInvoker


    override fun funnel(funnelFilter: FunnelReport.FunnelReportFilter): List<FunnelReport.FunnelStep> {
        logger.debug("Funnel data for funnelFilter : $funnelFilter")
        val clientID = AuthenticationUtils.clientID

        if(clientID == null){
            return emptyList()
        }

        val segmentUserIds = segmentService.segmentUserIds(funnelFilter.segmentid, clientID)
        val tz = userSettingsService.getTimeZone()
        val aggregation = buildAggregation(funnelFilter, clientID, segmentUserIds, tz)
        val userData = userAnalyticsRepository.funnelData(aggregation, clientID)

        val funnelData = FunnelData(userData, funnelFilter.steps.sortedBy { it -> it.order }.map { it -> it.eventName }, funnelFilter.conversionTime)
        val computedFunnels = awsFunnelLambdaInvoker.computeFunnels(funnelData)

        logger.debug("Funnel data computed: $computedFunnels")
        return computedFunnels
    }

    fun buildAggregation(funnelFilter: FunnelReport.FunnelReportFilter, clientID: Long, segmentUserIds: List<String>, tz: ZoneId): Aggregation {
        val filters = listOf(ReportUtil.buildFilter(GlobalFilterType.EventProperties, AggregationQuerybuilder.Field.UserId.fName, DataType.string, StringOperator.Contains.name, segmentUserIds, null),
                ReportUtil.buildFilter(GlobalFilterType.EventProperties, AggregationQuerybuilder.Field.EventName.fName, DataType.string, StringOperator.Contains.name, funnelFilter.steps.map { it -> it.eventName }, null))

        val filterGlobalQ = segmentParserCriteria.filterGlobalQ(filters, tz)
        val matchOperation = Aggregation.match(filterGlobalQ.first)

        val sortOperation = Aggregation.sort(Sort.by("creationTime").ascending())

        val groupBys = mutableListOf<Field>()
        groupBys.add(Fields.field(AggregationQuerybuilder.Field.UserId.fName, AggregationQuerybuilder.Field.UserId.fName))
        groupBys.add(Fields.field(AggregationQuerybuilder.Field.EventName.fName, AggregationQuerybuilder.Field.EventName.fName))
        val split = funnelFilter.splitProprty
        if(split != null && !funnelFilter.splitProprty.isNullOrBlank()) {
            val propertyPath = segmentParserCriteria.getFieldPath(funnelFilter.splitProprtyType, split)
            groupBys.add(Fields.field("attribute", propertyPath))
        }

        val groupByOperation1 = Aggregation.group(Fields.from(*groupBys.toTypedArray())).push("creationTime").`as`("chronology")

        val aggregationOperations = mutableListOf<AggregationOperation>()
        aggregationOperations.add(matchOperation)
        aggregationOperations.add(sortOperation)
        aggregationOperations.add(groupByOperation1)

        var pushObject = BasicDBObject("name", "\$_id.${AggregationQuerybuilder.Field.EventName.fName}").append("chronology", "\$chronology" )
        if(!funnelFilter.splitProprty.isNullOrBlank()) pushObject = pushObject.append("attribute", "\$_id.attribute")

        val groupByOperation2 = Aggregation.group("\$${AggregationQuerybuilder.Field.UserId.fName}").push(pushObject).`as`("eventChronology")
        aggregationOperations.add(groupByOperation2)

        return Aggregation.newAggregation(*aggregationOperations.toTypedArray())
    }
}