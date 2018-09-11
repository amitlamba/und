package com.und.service

import com.und.report.web.model.EventReport
import com.und.report.web.model.GroupBy
import com.und.web.model.*
import com.und.web.model.Unit
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.aggregation.*
import org.springframework.stereotype.Component
import java.time.ZoneId
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

const val NUM_OF_MINUTES = "numOfMinutes"
const val USER_DOC = "userDoc"
const val GROUP_BY_NAME = "groupByName"
const val USER_COUNT = "userCount"
const val AGGREGATE_VALUE = "aggregateValue"
const val TIME_PERIOD = "timePeriod"

@Component
@Deprecated("Replaced with more generic implementation AggregationQuerybuilder")
class MongoQueryUtil {

    //Define field names and their mapping.



    //Field details
    /**
     * Containing collection
     * Expression details
     * Match details
     * Grouping details
     */

    //Match options
    /**
     * Direct in event with values
     * Direct in user with values [addFields, lookup, unwind, match]
     * Event:
     *  Match with Computed (dateVal) [addFields, match]
     *  WithinLastNMinutes (now() - N minutes)
     * User:
     *
     *
     */

    //Group options
    /**
     * Direct
     *  Event count result [group]
     *  User count result  [group, project]
     *  Intermediate user count [group, unwind]
     * Computed field grouping [addFields, <above>]
     */


    //liveUser groupBy
    /**
     * Event property
     *  firstTime
     *  name
     *  geography.country
     *  geography.state
     *  system.os
     *  system.browser
     *  system.device
     *
     * User property
     *  standardInfo.country
     *  standardInfo.state
     *  standardInfo.gender
     */

    //fun buildUserIdMatchCriteria(userIds: List<String>): Criteria {
    //}

    enum class Collection{
        Event,
        User
    }

    enum class Field(val fName: String = "", val collectionName: Collection = Collection.Event, type: GlobalFilterType = GlobalFilterType.EventProperties, properties: List<String> = emptyList()) {
        EventName ("name", Collection.Event, GlobalFilterType.EventProperties),
        CreationTime ("creationTime", Collection.Event, GlobalFilterType.EventProperties),
        UserId ("userId", Collection.Event, GlobalFilterType.EventProperties),
        UserIdObject ("userIdObject", Collection.Event, GlobalFilterType.EventComputedProperties),
        TimeInMinutes("timeInMinutes", Collection.Event, GlobalFilterType.EventComputedProperties, listOf(NUM_OF_MINUTES)),
        DateVal("dateVal", Collection.Event, GlobalFilterType.EventComputedProperties),
        TimePeriod("timePeriod", Collection.Event, GlobalFilterType.EventComputedProperties)
    }

    private fun getAggregationExpression(field: Field, properties: Map<String, Any> = emptyMap()): AggregationExpression{
        return when(field){
            //{ $floor: {$divide: [{$add: [ {$multiply: [60, "$clientTime.hour"]}, "$clientTime.minute"]}, 5] } }
            // equivalent in Spel: ((clientTime.hour * 60) + (clientTime.minute - (clientTime.minute % 5)))/5
            Field.TimeInMinutes -> {
                ArithmeticOperators.Floor.floorValueOf(ArithmeticOperators.Divide.valueOf(ArithmeticOperators.Add.valueOf(ArithmeticOperators.Multiply.valueOf("clientTime.hour").multiplyBy(60)
                ).add("clientTime.minute")).divideBy(properties.get(NUM_OF_MINUTES).toString()))
            }
            Field.DateVal -> {
                DateOperators.dateOf("clientTime.time").toString("%Y-%m-%d")
            }
            Field.UserIdObject -> {
                ConvertOperators.ToObjectId.toObjectId("$${Field.UserId.fName}")
            }
            Field.TimePeriod -> {
                val timePeriod = properties.get(TIME_PERIOD) as EventReport.PERIOD
                when(timePeriod){
                    EventReport.PERIOD.weekly -> {
                        //TODO
                    }
                }

                ConvertOperators.ToObjectId.toObjectId("$${Field.UserId.fName}")
            }
            else -> {
                throw Exception("${field.fName} is not supported in computed fields")
            }
        }
    }

    @Autowired
    private lateinit var segmentParserCriteria: SegmentParserCriteria


    fun buildLiveUserAggregation(userIds: List<String>, groupBy: GroupBy, interval: Long, tz: ZoneId): Aggregation {
        var filters = mutableListOf<GlobalFilter>()
        var userIdFilter = buildFilter(GlobalFilterType.EventProperties, Field.UserId.fName, DataType.string, StringOperator.Contains.name, userIds, null)
        var creationTimeFilter = buildFilter(GlobalFilterType.EventProperties, Field.CreationTime.fName, DataType.date, DateOperator.InThePast.name,
                listOf(interval.toString()), Unit.mins)
        filters.add(userIdFilter)
        filters.add(creationTimeFilter)

        val filterGlobalQ = segmentParserCriteria.filterGlobalQ(filters, tz)
        val matchOperation = Aggregation.match(filterGlobalQ.first)


        val groupByFieldPath = segmentParserCriteria.getFieldPath(groupBy.globalFilterType)
        val groupByField = "$groupByFieldPath${groupBy.name}"
        val groupOperation = Aggregation.group(groupByField).addToSet(Field.UserId.fName).`as`(Field.UserId.fName)

        val projectOperation = Aggregation.project(Fields.from(Fields.field(groupByField))).and(Field.UserId.fName).size().`as`(AGGREGATE_VALUE)

        return Aggregation.newAggregation(matchOperation, groupOperation, projectOperation)
    }

    fun buildLiveUserTrendAggregation(userIds: List<String>, dates: List<String>, interval: Long, tz: ZoneId): Aggregation {
        //match
        var filters = mutableListOf<GlobalFilter>()
        val userIdFilter = buildFilter(GlobalFilterType.EventProperties, Field.UserId.fName, DataType.string, StringOperator.Contains.name, userIds, null)
        filters.add(userIdFilter)
        val filterCriterias = segmentParserCriteria.filterGlobalQ(filters, tz)
        val matchOperation = Aggregation.match(filterCriterias.first)

        //project
        val projectOperation1 = Aggregation.project(Fields.from(Fields.field(Field.UserId.fName)))
                .and(getAggregationExpression(Field.TimeInMinutes, mapOf(NUM_OF_MINUTES to interval))).`as`(Field.TimeInMinutes.fName)
                .and(getAggregationExpression(Field.DateVal)).`as`(Field.DateVal.fName)

        //match
        filters = mutableListOf<GlobalFilter>()
        val dateValFilter = buildFilter(GlobalFilterType.EventComputedProperties, Field.DateVal.fName, DataType.string, StringOperator.Contains.name, dates, null)
        filters.add(dateValFilter)
        val dateCriteria = segmentParserCriteria.filterGlobalQ(filters, tz)
        val dateMatch = Aggregation.match(dateCriteria.first)

        //group
        val groupOperation = Aggregation.group(Field.DateVal.fName, Field.TimeInMinutes.fName).addToSet(Field.UserId.fName).`as`(Field.UserId.fName)

        //project
        val projectOperation2 = Aggregation.project(Fields.fields(Field.DateVal.fName, Field.TimeInMinutes.fName)).and(Field.UserId.fName).size().`as`(AGGREGATE_VALUE)


        return Aggregation.newAggregation(matchOperation, projectOperation1, dateMatch, groupOperation, projectOperation2)
    }

    fun buildCountTrendAggregation(userIds: List<String>, requestFilter: EventReport.EventReportFilter, entityType: EventReport.EntityType, groupBy: GroupBy, tz: ZoneId, clientId: Long): Aggregation{
        //Possible pipelines
        /**
         * [match], [group]             (Event count using event-filters and event-groupBy)
         * [match], [group, project]    (User count using event-filters and event-groupBy)
         * [match], [project, lookup, unwind, match], [group]           (Event count using both-filters and any-groupBy)
         * [match], [project, lookup, unwind, match], [group, project]  (User count using both-filters and any-groupBy)
         *
         */

        val allFilters = segregateEventUserFilter(userIds, requestFilter)
        val userFilterPresent = allFilters.second.isNotEmpty()
        val userGroupByPresent = isUserCollection(groupBy.globalFilterType)

        //event match
        val eventFilterCriterias = segmentParserCriteria.filterGlobalQ(allFilters.first, tz)
        val eventMatchOperation = Aggregation.match(eventFilterCriterias.first)

        //join with user collection if needed
        val joinWithUserPipeline = mutableListOf<AggregationOperation>()
        val groupByFieldPath = segmentParserCriteria.getFieldPath(groupBy.globalFilterType)
        var groupByField = "$groupByFieldPath${groupBy.name}"

        if(userGroupByPresent) groupByField = "$USER_DOC.$groupByField"

        if(userFilterPresent || userGroupByPresent){
            var projectOperation = Aggregation.project(Field.UserId.fName)
                    .and(getAggregationExpression(Field.UserIdObject)).`as`(Field.UserIdObject.fName)
                    //.andExpression("toObjectId(userId)").`as`(Field.UserIdObject.fName)
                    //.andExpression("setEquals(userId, new int[]{5, 8, 13})").`as`("test")
            if(!userGroupByPresent) projectOperation = projectOperation.and(groupByField).`as`(groupByField)
            joinWithUserPipeline.add(projectOperation)

            val lookupOperation = Aggregation.lookup("${clientId}_eventUser", Field.UserIdObject.fName, "_id", USER_DOC)
            joinWithUserPipeline.add(lookupOperation)

            val unwindOperation = Aggregation.unwind(USER_DOC)
            joinWithUserPipeline.add(unwindOperation)

            if(userFilterPresent){
                val userFilterCriterias = segmentParserCriteria.filterGlobalQ(allFilters.second, tz)
                val userMatchOperation = Aggregation.match(userFilterCriterias.second)
                joinWithUserPipeline.add(userMatchOperation)
            }
        }

        //group
        val groupPipeline = mutableListOf<AggregationOperation>()
        if(entityType == EventReport.EntityType.user){
            val groupOperation = Aggregation.group().push(groupByField).`as`(groupBy.name).addToSet(Field.UserId.fName).`as`(Field.UserId.fName)
            val projectOperation = Aggregation.project(groupBy.name).and(Field.UserId.fName).size().`as`(AGGREGATE_VALUE)

            groupPipeline.add(groupOperation)
            groupPipeline.add(projectOperation)
        } else {
            val groupOperation = Aggregation.group(groupByField).count().`as`(AGGREGATE_VALUE)
            groupPipeline.add(groupOperation)
        }

        val totalPipeline = mutableListOf<AggregationOperation>()
        totalPipeline.add(eventMatchOperation)
        totalPipeline.addAll(joinWithUserPipeline)
        totalPipeline.addAll(groupPipeline)
        return Aggregation.newAggregation(*totalPipeline.toTypedArray())
    }

    private fun segregateEventUserFilter(userIds: List<String>, requestFilter: EventReport.EventReportFilter): Pair<List<GlobalFilter>, List<GlobalFilter>>{
        val eventFilters = mutableListOf<GlobalFilter>()
        val userFilters = mutableListOf<GlobalFilter>()

        val eventNameFilter = buildFilter(GlobalFilterType.EventProperties, Field.EventName.fName, DataType.string, StringOperator.Equals.name,
                listOf(requestFilter.eventName), null)
        eventFilters.add(eventNameFilter)

        val userIdFilter = buildFilter(GlobalFilterType.EventProperties, Field.UserId.fName, DataType.string, StringOperator.Contains.name, userIds, null)
        eventFilters.add(userIdFilter)

        val creationTimeFilter = buildFilter(GlobalFilterType.EventProperties, Field.CreationTime.fName, DataType.date, DateOperator.Between.name,
                listOf(requestFilter.fromDate, requestFilter.toDate), null)
        eventFilters.add(creationTimeFilter)

        requestFilter.propFilter.forEach{
            if(isUserCollection(it.globalFilterType)) userFilters.add(it) else eventFilters.add(it)
        }

        return Pair(eventFilters, userFilters)
    }

    private fun isUserCollection(globalFilterType: GlobalFilterType): Boolean{
        //TODO, move it to the enum so in case of a new entry in enum it doesn't get missed
        return globalFilterType in listOf(GlobalFilterType.UserProperties, GlobalFilterType.Demographics, GlobalFilterType.Reachability)
    }

    private fun buildFilter(globalFilterType: GlobalFilterType, name: String, type: DataType, operator: String, values: List<String>, valueUnit: Unit?): GlobalFilter{
        var filter = GlobalFilter()
        if(globalFilterType != null) filter.globalFilterType = globalFilterType
        if (name != null) filter.name = name
        if (type != null) filter.type = type
        if (operator != null) filter.operator = operator
        if (values != null) filter.values = values
        if (valueUnit != null) filter.valueUnit = valueUnit
        return filter
    }


}
