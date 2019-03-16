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
import com.und.web.model.*
import org.hibernate.internal.util.collections.CollectionHelper
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.aggregation.*
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter


const val FUNNEL_QUERY_PAGE_SIZE = 100

@Component
class FunnelReportServiceImpl : FunnelReportService {

    companion object {
        val logger: Logger = loggerFor(FunnelReportServiceImpl::class.java)
        const val allUser = ReportUtil.ALL_USER_SEGMENT
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


        return clientID?.let {

            val tz = userSettingsService.getTimeZone()
            val aggregation = if (funnelFilter.segmentid == allUser) {
                buildAggregationAllUsers(funnelFilter, clientID, tz)
            } else {
                val segmentUserIds = segmentService.segmentUserIds(funnelFilter.segmentid, clientID)
                buildAggregation(funnelFilter, clientID, segmentUserIds, tz)
            }

            val userData = userAnalyticsRepository.funnelData(aggregation, clientID)

            val funnelData = FunnelData(userData, funnelFilter.steps.sortedBy { it.order }.map { it.eventName }, funnelFilter.conversionTime)
            val computedFunnels = awsFunnelLambdaInvoker.computeFunnels(funnelData)

            logger.debug("Funnel data computed: $computedFunnels")
            return if(funnelFilter.filters.isEmpty()) fillMissingSteps(orderFunnelByStep(computedFunnels),funnelFilter.steps)
            else orderFunnelByStep(computedFunnels)
        } ?: emptyList()
    }
    //This code should be synchronized its possible that two thread invoke that method at same time
    private fun orderFunnelByStep(result:List<FunnelReport.FunnelStep>):List<FunnelReport.FunnelStep>{
        var funnelResult=result.toMutableList()
        for (i in 0..(funnelResult.size - 1) step 1) {

            var min = funnelResult[i].step.order

            for (j in (i + 1)..(funnelResult.size-1) step 1) {
                if (min > funnelResult[j].step.order) {
                    var temp = funnelResult[j]
                    funnelResult[j] = funnelResult[i]
                    funnelResult[i] = temp
                    min = temp.step.order
                }
            }
        }

        return funnelResult
    }
    // This code should be synchronized its possible that two thread invoke that method at same time
    private fun fillMissingSteps(result:List<FunnelReport.FunnelStep>,funnelSteps:List<FunnelReport.Step>):List<FunnelReport.FunnelStep>{
        val outputSize=result.size
        val inputSize=funnelSteps.size
        if(outputSize!=inputSize){
            var rs=result.toMutableList()
            for(i in outputSize..(inputSize-1) step 1){
                rs.add(FunnelReport.FunnelStep(funnelSteps[i],count = 0,property = "all"))
            }
            return rs
        }
        return result
    }

    fun buildAggregationAllUsers(funnelFilter: FunnelReport.FunnelReportFilter, clientID: Long, tz: ZoneId): Aggregation {

        val filters = listOf(
                ReportUtil.buildFilter(GlobalFilterType.EventProperties, AggregationQuerybuilder.Field.EventName.fName, DataType.string, StringOperator.Contains.name, funnelFilter.steps.map { it -> it.eventName }, null)
        )
        return buildAggregation(funnelFilter, filters, tz)
    }


    fun buildAggregation(funnelFilter: FunnelReport.FunnelReportFilter, clientID: Long, segmentUserIds: List<String>, tz: ZoneId): Aggregation {
        val filters = listOf(
                ReportUtil.buildFilter(GlobalFilterType.EventProperties, AggregationQuerybuilder.Field.UserId.fName, DataType.string, StringOperator.Contains.name, segmentUserIds, null),
                ReportUtil.buildFilter(GlobalFilterType.EventProperties, AggregationQuerybuilder.Field.EventName.fName, DataType.string, StringOperator.Contains.name, funnelFilter.steps.map { it -> it.eventName }, null)
        )

        return buildAggregation(funnelFilter, filters, tz)
    }

    private fun buildAggregation(funnelFilter: FunnelReport.FunnelReportFilter, filters: List<GlobalFilter>, tz: ZoneId): Aggregation {

        val campaignId=funnelFilter.filters[0].values[0].toLong()
        val allfilters :MutableList<GlobalFilter> = mutableListOf()

        allfilters.addAll(filters)
//        allfilters.addAll(funnelFilter.filters)
        val dateFilter = createDateFilter(tz, funnelFilter)
        allfilters.add(dateFilter)
        val filterGlobalQ = segmentParserCriteria.filterGlobalQ(allfilters, tz)

        val matchOperation = Aggregation.match(filterGlobalQ.first)
        val sortOperation = Aggregation.sort(Sort.by(AggregationQuerybuilder.Field.CreationTime.fName).ascending())
        val groupBys = mutableListOf<Field>()
        groupBys.add(Fields.field(AggregationQuerybuilder.Field.UserId.fName, AggregationQuerybuilder.Field.UserId.fName))
        groupBys.add(Fields.field(AggregationQuerybuilder.Field.EventName.fName, AggregationQuerybuilder.Field.EventName.fName))
        val split = funnelFilter.splitProperty
        var c=ConvertOperators.ConvertOperatorFactory(AggregationQuerybuilder.Field.CreationTime.fName).convertToLong()
        var projectionOperation= Aggregation.project("userId","name").and(c).`as`(AggregationQuerybuilder.Field.CreationTime.fName)
        var propertyPath:String
        if (split != null && !funnelFilter.splitProperty.isNullOrBlank()) {
            propertyPath = segmentParserCriteria.getFieldPath(funnelFilter.splitPropertyType, split)
            groupBys.add(Fields.field("attribute", "splitproperty"))
            projectionOperation=projectionOperation.and(propertyPath).`as`("splitproperty")
        }

        val groupByOperation1 = Aggregation.group(Fields.from(*groupBys.toTypedArray())).push(AggregationQuerybuilder.Field.CreationTime.fName).`as`("chronology")

        val aggregationOperations = mutableListOf<AggregationOperation>()
        aggregationOperations.add(matchOperation)
        if(funnelFilter.filters.isNotEmpty()) {
            var attributeMatch = Aggregation.match(Criteria().orOperator(Criteria().andOperator(
                    Criteria.where("name").`is`(funnelFilter.steps[0].eventName),
                    Criteria("attributes.campaign_id").`is`(campaignId)), Criteria("name").`is`(funnelFilter.steps[1].eventName)))
            aggregationOperations.add(attributeMatch)
        }
        aggregationOperations.add(sortOperation)
        aggregationOperations.add(projectionOperation)
        aggregationOperations.add(groupByOperation1)

        var pushObject = BasicDBObject("Event", "\$_id.${AggregationQuerybuilder.Field.EventName.fName}").append("chronology", "\$chronology" )
        if(!funnelFilter.splitProperty.isNullOrBlank()) pushObject = pushObject.append("attribute", "\$_id.attribute")

        val groupByOperation2 = Aggregation.group("\$${AggregationQuerybuilder.Field.UserId.fName}").push(pushObject).`as`("chronologies")
        aggregationOperations.add(groupByOperation2)
        return Aggregation.newAggregation(*aggregationOperations.toTypedArray())
    }

    private fun createDateFilter(tz: ZoneId, funnelFilter: FunnelReport.FunnelReportFilter): GlobalFilter {
        val dateFilter = GlobalFilter()
        dateFilter.globalFilterType = GlobalFilterType.EventProperties
        dateFilter.name = AggregationQuerybuilder.Field.CreationTime.fName
        dateFilter.type = DataType.date
        dateFilter.operator = DateOperator.After.name
        val date = LocalDate.now(tz).minusDays(funnelFilter.days)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedString = date.format(formatter)
        dateFilter.values += formattedString
        return dateFilter
    }
}