package com.und.service

import com.und.report.web.model.AggregateBy
import com.und.report.web.model.EventReport
import com.und.report.web.model.GroupBy
import com.und.web.model.*
import com.und.web.model.Unit
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.aggregation.*
import org.springframework.stereotype.Component
import java.time.ZoneId

@Component
class AggregationQuerybuilder {

    enum class Collection{
        Event,
        User
    }

    enum class Field(val fName: String = "", val collectionName: Collection = Collection.Event, type: GlobalFilterType = GlobalFilterType.EventProperties, properties: List<String> = emptyList()) {
        EventName ("name", Collection.Event, GlobalFilterType.EventProperties),
        CreationTime ("creationTime", Collection.Event, GlobalFilterType.EventProperties),
        UserId ("userId", Collection.Event, GlobalFilterType.EventProperties),
        UserIdObject ("userIdObject", Collection.Event, GlobalFilterType.EventComputedProperties),
        MinutesPeriod("minutesPeriod", Collection.Event, GlobalFilterType.EventComputedProperties, listOf(NUM_OF_MINUTES)),
        DateVal("dateVal", Collection.Event, GlobalFilterType.EventComputedProperties),
        TimePeriod("timePeriod", Collection.Event, GlobalFilterType.EventComputedProperties),
        Hour("hour", Collection.Event, GlobalFilterType.EventTimeProperties),
        UserType("userType", Collection.User, GlobalFilterType.UserComputedProperties),
    }

    @Autowired
    private lateinit var segmentParserCriteria: SegmentParserCriteria

    fun getAggregationExpression(fieldName: String, properties: Map<String, Any> = emptyMap()): AggregationExpression {
        return when(fieldName){
            //{ $floor: {$divide: [{$add: [ {$multiply: [60, "$clientTime.hour"]}, "$clientTime.minute"]}, 5] } }
            // equivalent in Spel: ((clientTime.hour * 60) + (clientTime.minute - (clientTime.minute % 5)))/5
            Field.MinutesPeriod.fName -> {
                val numOfMinutes = properties.get(NUM_OF_MINUTES).toString().toInt()
                ArithmeticOperators.Floor.floorValueOf(ArithmeticOperators.Divide.valueOf(ArithmeticOperators.Add.valueOf(ArithmeticOperators.Multiply.valueOf("clientTime.hour").multiplyBy(60)
                ).add("clientTime.minute")).divideBy(numOfMinutes))
            }
            Field.DateVal.fName -> {
                DateOperators.dateOf("clientTime.time").toString("%Y-%m-%d")
            }
            Field.UserIdObject.fName -> {
                ConvertOperators.ToObjectId.toObjectId("$${Field.UserId.fName}")
            }
            Field.UserType.fName -> {
                val eventDateSameAsUserCreationDate = ComparisonOperators.Eq.valueOf(Field.DateVal.fName).equalTo(DateOperators.dateOf("userDoc.creationTime").toString("%Y-%m-%d"))
                ConditionalOperators.`when`(eventDateSameAsUserCreationDate).then("new").otherwise("old")
            }
            else -> {
                throw Exception("${fieldName} is not supported in computed fields")
            }
        }
    }

    private fun getCompleteScopedName(name: String, globalFilterType: GlobalFilterType): String {
        val fieldPath = segmentParserCriteria.getFieldPath(globalFilterType,name)
        if(isUserCollection(globalFilterType)) return "$USER_DOC.$fieldPath"
        else return "$fieldPath"
    }

    fun buildAggregationPipeline(filters: List<GlobalFilter>, groupBys: List<GroupBy>, aggregateBy: AggregateBy?, paramValues: Map<String, Any> = emptyMap(), entityType: EventReport.EntityType, tz: ZoneId, clientId: Long): List<AggregationOperation> {
        //Possible pipelines
        /**
         * [match], [group]             (Event count using event-filters and event-groupBy)
         * [match], [group, project]    (User count using event-filters and event-groupBy)
         * [match], [project, lookup, unwind, match], [group]           (Event count using both-filters and any-groupBy)
         * [match], [project, lookup, unwind, match], [group, project]  (User count using both-filters and any-groupBy)
         *
         */

        val allFilters = segregateEventUserFilter(filters)
        val allGroupBys = segregateEventUserGroupBy(groupBys)
        val aggregationPipeline = mutableListOf<AggregationOperation>()

        //event match
        val eventFilterCriterias = segmentParserCriteria.filterGlobalQ(allFilters.eventSimpleFilters, tz)
        if(eventFilterCriterias.first != null){
            val eventMatchOperation = Aggregation.match(eventFilterCriterias.first)
            aggregationPipeline.add(eventMatchOperation)
        }

        //computed event match
        val eventProjectionRequired = allFilters.eventComputedFilters.isNotEmpty() || allGroupBys.eventComputedGroupBys.isNotEmpty()
                || (aggregateBy != null && !isUserCollection(aggregateBy.globalFilterType))
        if(eventProjectionRequired){
            var projectOperation = Aggregation.project(Field.UserId.fName)

            allFilters.eventComputedFilters.forEach {
                projectOperation = projectOperation.and(getAggregationExpression(it.name, paramValues)).`as`(it.name)
            }

            allGroupBys.eventSimpleGroupBys.forEach {
                val scopedName = getCompleteScopedName(it.groupName, it.groupFilterType)
                projectOperation = projectOperation.and(scopedName).`as`(scopedName)
            }

            //add those computed event groupBy only which are not in computed event filters
            allGroupBys.eventComputedGroupBys.filter {group -> !allFilters.eventComputedFilters.map { it.name }.contains(group.groupName)}.forEach {
                projectOperation = projectOperation.and(getAggregationExpression(it.groupName, paramValues)).`as`(it.groupName)
            }

            if(aggregateBy != null && !isUserCollection(aggregateBy.globalFilterType)){
                val scopedName = getCompleteScopedName(aggregateBy.name, aggregateBy.globalFilterType)
                projectOperation = projectOperation.and(scopedName).`as`(scopedName)
            }

            aggregationPipeline.add(projectOperation)

            if(allFilters.eventComputedFilters.isNotEmpty()){
                val eventComputedFilterCriterias = segmentParserCriteria.filterGlobalQ(allFilters.eventComputedFilters, tz)
                val eventComputedMatchOperation = Aggregation.match(eventComputedFilterCriterias.first)
                aggregationPipeline.add(eventComputedMatchOperation)
            }
        }

        //group by, unwind for event
        val eventGroupByPresent = allGroupBys.eventComputedGroupBys.isNotEmpty() || allGroupBys.eventSimpleGroupBys.isNotEmpty()
        val eventAggregateByPresent = (aggregateBy != null) && !isUserCollection(aggregateBy.globalFilterType)
        val userFilterPresent = allFilters.userComputedFilters.isNotEmpty() || allFilters.userSimpleFilters.isNotEmpty()
        val userGroupByPresent = allGroupBys.userComputedGroupBys.isNotEmpty() || allGroupBys.userSimpleGroupBys.isNotEmpty()
        val userAggregateByPresent = (aggregateBy != null) && isUserCollection(aggregateBy.globalFilterType)

        val eventGroupFields = mutableMapOf<String, String>()
        val eventOutputJoinWithUser = (entityType == EventReport.EntityType.event && (eventGroupByPresent || eventAggregateByPresent))
                && (userFilterPresent || userGroupByPresent || userAggregateByPresent)
        if(eventGroupByPresent){
            eventGroupFields.putAll(allGroupBys.eventSimpleGroupBys.map { it.groupName to getCompleteScopedName(it.groupName, it.groupFilterType)}.toMap())
            eventGroupFields.putAll(allGroupBys.eventComputedGroupBys.map { it.groupName to it.groupName}.toMap())

            /**
             * User count output: put userId in a set
             * Event count output & (user filter or user group by): group by userId to a user-count
             * Event count output with no user filter/group by: count events to result [last point of pipeline]
             */
            val fields = eventGroupFields.map { it -> Fields.field(it.key, it.value) }.toTypedArray()
            var eventGroupOperation = Aggregation.group(Fields.from(*fields))

            if(eventOutputJoinWithUser) {
                if(eventAggregateByPresent && aggregateBy!=null){
                    val scopedName = getCompleteScopedName(aggregateBy.name, aggregateBy.globalFilterType)
                    when(aggregateBy.aggregationType){
                        AggregationType.Sum -> eventGroupOperation = Aggregation.group(Fields.from(*fields, Fields.field(Field.UserId.fName))).sum(scopedName).`as`(AGGREGATE_VALUE)
                        AggregationType.Avg -> eventGroupOperation = Aggregation.group(Fields.from(*fields, Fields.field(Field.UserId.fName))).avg(scopedName).`as`(AGGREGATE_VALUE)
                    }
                }
                else eventGroupOperation = Aggregation.group(Fields.from(*fields, Fields.field(Field.UserId.fName))).count().`as`(USER_COUNT)
            }
            else if(entityType == EventReport.EntityType.user)
                eventGroupOperation = eventGroupOperation.addToSet(Field.UserId.fName).`as`(Field.UserId.fName)
            else {//last operation of pipeline
                if(eventAggregateByPresent && aggregateBy!=null){
                    val scopedName = getCompleteScopedName(aggregateBy.name, aggregateBy.globalFilterType)
                    when(aggregateBy.aggregationType){
                        AggregationType.Sum -> eventGroupOperation = eventGroupOperation.sum(scopedName).`as`(AGGREGATE_VALUE)
                        AggregationType.Avg -> eventGroupOperation = eventGroupOperation.avg(scopedName).`as`(AGGREGATE_VALUE)
                    }
                }
                else eventGroupOperation = eventGroupOperation.count().`as`(AGGREGATE_VALUE)
            }

            aggregationPipeline.add(eventGroupOperation)

            if(entityType == EventReport.EntityType.user && (userFilterPresent || userGroupByPresent)){
                val unwindOperation = Aggregation.unwind(Field.UserId.fName)
                aggregationPipeline.add(unwindOperation)
            }
        }

        //join with user collection if needed
        val userGroupFields = mutableMapOf<String, String>()
        if(userFilterPresent || userGroupByPresent){
            val projectionFields = mutableListOf<String>()

            projectionFields.add(Field.UserId.fName)

            if(eventOutputJoinWithUser){
                if(eventAggregateByPresent) projectionFields.add(AGGREGATE_VALUE)
                else projectionFields.add(USER_COUNT)
            }

            var projectOperation = Aggregation.project(*projectionFields.toTypedArray())

            if(entityType == EventReport.EntityType.user && eventGroupFields.size == 1) projectOperation = projectOperation.and("_id").`as`(eventGroupFields.values.first())
            else eventGroupFields.forEach { t, u ->   projectOperation = projectOperation.and(t).`as`(u)}

            if(eventOutputJoinWithUser) projectOperation = projectOperation.and(ConvertOperators.ToObjectId.toObjectId("\$_id.${Field.UserId.fName}")).`as`(Field.UserIdObject.fName)
            else projectOperation = projectOperation.and(getAggregationExpression(Field.UserIdObject.fName)).`as`(Field.UserIdObject.fName)


            aggregationPipeline.add(projectOperation)

            val lookupOperation = Aggregation.lookup("${clientId}_eventUser", "${Field.UserIdObject.fName}", "_id", USER_DOC)
            aggregationPipeline.add(lookupOperation)

            val unwindOperation = Aggregation.unwind(USER_DOC)
            aggregationPipeline.add(unwindOperation)

            if(userFilterPresent){
                //TODO handling for computed user filters
                val userFilterCriterias = segmentParserCriteria.joinAwareFilterGlobalQ(allFilters.userSimpleFilters, tz, null,true)
                val userMatchOperation = Aggregation.match(userFilterCriterias.second)
                aggregationPipeline.add(userMatchOperation)
            }

            if(eventGroupByPresent || userGroupByPresent){
                //TODO handling for computed user group by
                userGroupFields.putAll(allGroupBys.userSimpleGroupBys.map { it.groupName to getCompleteScopedName(it.groupName, it.groupFilterType)}.toMap())
                val allGroupByFields = mutableListOf<String>()
                allGroupByFields.addAll(eventGroupFields.values)
                allGroupByFields.addAll(userGroupFields.values)
                var userGroupOperation = Aggregation.group(*allGroupByFields.toTypedArray())

                if(entityType == EventReport.EntityType.user) {
                    if(userAggregateByPresent && aggregateBy != null){
                        val scopedName = getCompleteScopedName(aggregateBy.name, aggregateBy.globalFilterType)
                        when(aggregateBy.aggregationType){
                            AggregationType.Sum -> userGroupOperation = userGroupOperation.sum(scopedName).`as`(AGGREGATE_VALUE)
                            AggregationType.Avg -> userGroupOperation = userGroupOperation.avg(scopedName).`as`(AGGREGATE_VALUE)
                        }
                    }
                    else userGroupOperation = userGroupOperation.addToSet(Field.UserId.fName).`as`(Field.UserId.fName)
                }
                else if (eventOutputJoinWithUser){//last operation of pipeline
                    if(eventAggregateByPresent && aggregateBy != null){
                        val scopedName = getCompleteScopedName(aggregateBy.name, aggregateBy.globalFilterType)
                        when(aggregateBy.aggregationType){
                            AggregationType.Sum -> userGroupOperation = userGroupOperation.sum(AGGREGATE_VALUE).`as`(AGGREGATE_VALUE)
                            AggregationType.Avg -> userGroupOperation = userGroupOperation.avg(AGGREGATE_VALUE).`as`(AGGREGATE_VALUE)
                        }
                    }
                    else userGroupOperation = userGroupOperation.sum(USER_COUNT).`as`(AGGREGATE_VALUE)
                }
                else
                    userGroupOperation = userGroupOperation.count().`as`(AGGREGATE_VALUE)
                aggregationPipeline.add(userGroupOperation)
            }
        }

        //final output if not allready pushed in pipeline
        if(entityType == EventReport.EntityType.user && !userAggregateByPresent){
//            val allGroupByFields = mutableMapOf<String, String>()
//            allGroupByFields.putAll(eventGroupFields)
//            allGroupByFields.putAll(userGroupFields)

            var resultProjectOperation = Aggregation.project().and(Field.UserId.fName).size().`as`(AGGREGATE_VALUE)
//            if(allGroupByFields.size > 1) {
//                allGroupByFields.forEach { t, u -> resultProjectOperation = resultProjectOperation.and(t).`as`(u) }
//                resultProjectOperation = resultProjectOperation.and(Field.UserId.fName).size().`as`(AGGREGATE_VALUE)
//            }

            aggregationPipeline.add(resultProjectOperation)
        }

        return aggregationPipeline
    }

    fun buildAggregation(filters: List<GlobalFilter>, groupBys: List<GroupBy>, aggregateBy: AggregateBy?, paramValues: Map<String, Any> = emptyMap(), entityType: EventReport.EntityType, tz: ZoneId, clientId: Long): Aggregation {
        return Aggregation.newAggregation(*buildAggregationPipeline(filters, groupBys, aggregateBy, paramValues, entityType, tz, clientId).toTypedArray())
    }


    private fun segregateEventUserFilter(filters: List<GlobalFilter>): FilterHolder{
        val filterHolder = FilterHolder()
        if(filters.isEmpty()) return filterHolder


        filters.forEach{
            if(isUserCollection(it.globalFilterType)){
                if(isComputedProperty(it.globalFilterType)) filterHolder.userComputedFilters.add(it) else filterHolder.userSimpleFilters.add(it)
            } else {
                if(isComputedProperty(it.globalFilterType)) filterHolder.eventComputedFilters.add(it) else filterHolder.eventSimpleFilters.add(it)
            }
        }

        return filterHolder
    }

    private fun segregateEventUserGroupBy(groupBys: List<GroupBy>): GroupByHolder{
        val groupByHolder = GroupByHolder()
        if(groupBys.isEmpty()) return groupByHolder


        groupBys.forEach{
            if(isUserCollection(it.groupFilterType)){
                if(isComputedProperty(it.groupFilterType)) groupByHolder.userComputedGroupBys.add(it) else groupByHolder.userSimpleGroupBys.add(it)
            } else {
                if(isComputedProperty(it.groupFilterType)) groupByHolder.eventComputedGroupBys.add(it) else groupByHolder.eventSimpleGroupBys.add(it)
            }
        }

        return groupByHolder
    }

    private fun isUserCollection(globalFilterType: GlobalFilterType): Boolean{
        //TODO, move it to the enum so in case of a new entry in enum it doesn't get missed
        return globalFilterType in listOf(GlobalFilterType.UserProperties, GlobalFilterType.Demographics, GlobalFilterType.Reachability, GlobalFilterType.UserComputedProperties)
    }

    private fun isComputedProperty(globalFilterType: GlobalFilterType): Boolean {
        //TODO, move it to the enum so in case of a new entry in enum it doesn't get missed
        return globalFilterType in listOf(GlobalFilterType.UserComputedProperties, GlobalFilterType.EventComputedProperties)
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


    class FilterHolder {
        val eventSimpleFilters = mutableListOf<GlobalFilter>()
        val eventComputedFilters = mutableListOf<GlobalFilter>()

        val userSimpleFilters = mutableListOf<GlobalFilter>()
        val userComputedFilters = mutableListOf<GlobalFilter>()
    }

    class GroupByHolder {
        val eventSimpleGroupBys = mutableListOf<GroupBy>()
        val eventComputedGroupBys= mutableListOf<GroupBy>()

        val userSimpleGroupBys= mutableListOf<GroupBy>()
        val userComputedGroupBys= mutableListOf<GroupBy>()
    }
}