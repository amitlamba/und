package com.und.service

import com.und.common.utils.DateUtils
import com.und.common.utils.loggerFor
import com.und.web.model.*
import kotlin.Unit
import org.slf4j.Logger
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.aggregation.*
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.aggregation.GroupOperation
import org.springframework.data.mongodb.core.aggregation.MatchOperation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Component
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/*
1. parse events, didn't do and do
2. parse use properties
3. parse geography
4. check data type of property and use comparator's properly
5. use aggregate query for count, sum of and first time etc.
6. and in the end convert this to valid mongo query(one for events and other for user properties)
7. create proper mongo indexes
8. combine results of both
9. cache and store parsed query


In case userId is not null of the segment, the aim becomes whether this userId belongs to the segment or not (so all the queries are with userId match)
 */

@Component
class SegmentParserCriteria {

    companion object {
        val logger: Logger = loggerFor(SegmentParserCriteria::class.java)
    }


    var userMap:HashMap<String,String>
    var eventMap:HashMap<String,String>

    constructor(){
        userMap=HashMap();
        userMap.put("gender","standardInfo.gender")
        userMap.put("dob","standardInfo.dob")
        userMap.put("country","standardInfo.country")
        userMap.put("state","standardInfo.state")
        userMap.put("countryCode","standardInfo.countryCode")
        userMap.put("City","standardInfo.City")
        userMap.put("emailIdentity","identity.email")
        userMap.put("mobileIdentity","identity.mobile")
        userMap.put("emailReach","communication.email.dnd")
        userMap.put("mobileReach","communication.mobile.dnd")
        userMap.put("creationTime","creationTime")
        userMap.put("os","system.os.name")
        userMap.put("browser","system.browser.name")
        userMap.put("device","system.device.name")

        eventMap=HashMap();
        eventMap.put("country","geogrophy.country")
        eventMap.put("state","geogrophy.state")
        eventMap.put("city","geogrophy.city")
        eventMap.put("clientTime","clientTime")
        eventMap.put("name","name")
        eventMap.put("os","system.os.name")
        eventMap.put("browser","system.browser.name")
        eventMap.put("device","system.device.name")
        eventMap.put("creationTime","creationTime")


    }

    private val dateUtils = DateUtils()

    enum class Field(val fName: String = "") {
        eventName("name"),
        creationTime,
        clientId,
        userId,
        hour,
        minute,
        weekday,
        monthday,
        month,
        year,
        count,
        sumof,
        firstTime

    }


    fun segmentQueries(segment: Segment, tz: ZoneId): SegmentQuery {


        val geoCriteria = if (segment.geographyFilters.isNotEmpty()) segment.geographyFilters.let { geoFilter -> filterGeography(geoFilter, segment.userId) } else null

        val gFilters = segment.globalFilters
        val (eventPropertyMatch, userPropertyMatch) = filterGlobalQWithUserId(gFilters, tz, segment.userId)

        val userQuery = if (userPropertyMatch != null) parseUsers(userPropertyMatch) else null

        //is this condition come true.
        val userCriteria = if (eventPropertyMatch == null && segment.userId != null) Criteria.where("userId").`is`(segment.userId) else null

        //did
        val did = segment.didEvents
        val didq =
                did?.let { Pair(parseEvents(it.events, tz, true, eventPropertyMatch, geoCriteria, userCriteria), it.joinCondition.conditionType) }
                        ?: Pair(emptyList(), ConditionType.AllOf)

        //and not
        val didnot = segment.didNotEvents
        val didnotq = didnot?.let { Pair(parseEvents(it.events, tz, false, null, null, userCriteria), ConditionType.AnyOf) }
                ?: Pair(emptyList(), ConditionType.AnyOf)

        if (didq.first.isEmpty()) {
            return SegmentQuery(didq, didnotq, userQuery, geoCriteria)
        } else {
            return SegmentQuery(didq, didnotq, userQuery)
        }
    }


    fun segmentQuery1(segment: Segment, tz: ZoneId, type:String): Pair<List<AggregationOperation>,Boolean>{
        var onlyEventUser=false
        var listOfAggregation = mutableListOf<AggregationOperation>()
        /*
        * Geography based aggregation
        * */
        val geoCriteria = if (segment.geographyFilters.isNotEmpty()) segment.geographyFilters.let { geoFilter -> filterGeography(geoFilter, segment.userId) } else null
        geoCriteria?.let {
            var geoCriteriaAgg = Aggregation.match(it)
            listOfAggregation.add(geoCriteriaAgg)
        }


        /*
        * divide globalfilter into event and user and find criteria
        * */
        val gFilters = segment.globalFilters
        val (eventPropertyMatch, userPropertyMatch) = filterGlobalQWithUserId(gFilters, tz, segment.userId)
        //adding event globalCriteria aggregation
        if (eventPropertyMatch != null)
            listOfAggregation.add(Aggregation.match(eventPropertyMatch))

        //add didnot
        addDidNotAggregation(segment, listOfAggregation, tz)
        //add did
        if(geoCriteria==null&&eventPropertyMatch==null && listOfAggregation.isEmpty()){
            addDidAggregationWithoutFacet(segment,listOfAggregation,tz)
        }else{
            addDidAggregation(segment, listOfAggregation, tz)
        }
        if(!listOfAggregation.isEmpty()){
            //common to all after did
            var group = Aggregation.group("userId")
            var convertToOBjectId = ConvertOperators.ConvertOperatorFactory("_id").convertToObjectId()
            var project = Aggregation.project().and(convertToOBjectId).`as`("_id")
            var sort = Aggregation.sort(Sort.Direction.ASC, "_id")
            var lookup = Aggregation.lookup("3_eventUser", "_id", "_id", "user")
            var unwindOperation = Aggregation.unwind("user")
            var replaceRootOperation = Aggregation.replaceRoot("user")

            listOfAggregation.add(group)
            listOfAggregation.add(project)
            listOfAggregation.add(sort)
            listOfAggregation.add(lookup)
            listOfAggregation.add(unwindOperation)
            listOfAggregation.add(replaceRootOperation)
        }else{
            onlyEventUser=true
        }
        //adding user globalcriteria aggregation
        if (userPropertyMatch != null) {
            listOfAggregation.add(Aggregation.match(userPropertyMatch))
        }
        if(type.equals("userId")){
            var convertor=ConvertOperators.ConvertOperatorFactory("_id").convertToString()
            listOfAggregation.add(Aggregation.project().and(convertor).`as`("_id"))
            listOfAggregation.add(Aggregation.group().push("_id").`as`("userId"))
        }

        return Pair(listOfAggregation,onlyEventUser)
    }

    private fun addDidNotAggregation(segment: Segment, listOfAggregation: MutableList<AggregationOperation>, tz: ZoneId) {
        var didnot = segment.didNotEvents?.events
        var listOfDidNotCriteria = mutableListOf<Criteria>()
        didnot?.forEachIndexed { index, event ->
            listOfDidNotCriteria.add(parseEvents2(event, tz, false))
        }
        if(listOfDidNotCriteria.isNotEmpty()){
            var matchOperation = Aggregation.match(Criteria().norOperator(Criteria().orOperator(*listOfDidNotCriteria.toTypedArray())))
            listOfAggregation.add(matchOperation)
        }

    }

    private fun addDidAggregation(segment: Segment, listOfAggregation: MutableList<AggregationOperation>, tz: ZoneId) {
        var did = segment.didEvents?.events
        var didAgg = Aggregation.facet()
        did?.forEachIndexed { index, event ->
            var a = parseEvents1(event, tz, true)
            var aggregationMatchOperation = a.get(0)
            var aggGroupOperation = a.get(1)
            var aggMatchOperation = a.get(2)
            didAgg = didAgg.and(aggregationMatchOperation, aggGroupOperation, aggMatchOperation).`as`("pipe" + index)
        }
        var setIntersection: SetOperators.SetIntersection
        var setUnion:SetOperators.SetUnion
        did?.let{
            if (it.size >= 2) {
                var afterFacetStage:ProjectionOperation
                if(segment.didEvents!!.joinCondition.conditionType.equals("AllOf")) {
                    setIntersection = SetOperators.SetOperatorFactory("pipe0._id").intersects("pipe1._id")

                    did?.forEachIndexed { index, event ->
                        if (index > 1) {
                            setIntersection = setIntersection.intersects("pipe" + index + "._id")
                        }
                    }

                    afterFacetStage = Aggregation.project().and(setIntersection).`as`("userId")
                }else{
                    setUnion = SetOperators.SetOperatorFactory("pipe0._id").union("pipe1._id")

                    did?.forEachIndexed { index, event ->
                        if (index > 1) {
                            setUnion = setUnion.union("pipe" + index + "._id")
                        }
                    }
                    afterFacetStage=Aggregation.project().and(setUnion).`as`("userId")
                }
            
            var unwindAfterFacetStage = Aggregation.unwind("userId")

            listOfAggregation.add(didAgg)
            listOfAggregation.add(afterFacetStage)
            listOfAggregation.add(unwindAfterFacetStage)

        } else if (it.size == 1) {
            var project = Aggregation.project().and("pipe0._id").`as`("userId")
            var unwindAfterFacetStage = Aggregation.unwind("userId")
            listOfAggregation.add(didAgg)
            listOfAggregation.add(project)
            listOfAggregation.add(unwindAfterFacetStage)
        } else{

            }

        }
    }

    private fun addDidAggregationWithoutFacet(segment: Segment, listOfAggregation: MutableList<AggregationOperation>, tz: ZoneId){
        var did = segment.didEvents?.events
        var listOfCriteria= mutableListOf<Criteria>()
        did?.forEachIndexed{index, event ->
             var matches=getListOfCriteria(event,tz)
            var criteria=Criteria().andOperator(*matches.toTypedArray())
            listOfCriteria.add(criteria)
        }

        listOfAggregation.add(Aggregation.match(Criteria().orOperator(*listOfCriteria.toTypedArray())))
        //add facet


    }
    private fun getListOfCriteria(event: Event,tz: ZoneId):MutableList<Criteria>{
        var matches = mutableListOf<Criteria>()
        matches.addAll(parsePropertyFilters(event, tz))
        matches.add(Criteria.where(Field.eventName.fName).`is`(event.name))
        matches.add(Criteria.where("userId").exists(true))
        matches.add(parseDateFilter(event.dateFilter, tz))
        return matches
    }
    private fun parseEvents1(event: Event, tz: ZoneId, did: Boolean): List<AggregationOperation> {

        var listOfAggregationOperation = mutableListOf<AggregationOperation>()
        var matches = mutableListOf<Criteria>()
        matches=getListOfCriteria(event,tz)
        var fields = Aggregation.fields(Field.userId.name, Field.creationTime.name, Field.clientId.name)
        matches.forEach { criteria ->
            val name = criteria.key
            if (name != null) {
                fields = fields.and(name, name)
            }
        }
        val matchOps = Aggregation.match(Criteria().andOperator(*matches.toTypedArray()))
        listOfAggregationOperation.add(matchOps)
        val whereCond = if (did) {
            event.whereFilter?.let { whereFilter -> whereFilterParse(whereFilter, tz) } ?: Optional.empty()
        } else Optional.empty()

        if (whereCond.isPresent) {
            val group = whereCond.get().first
            val matchOnGroup = whereCond.get().second
            listOfAggregationOperation.add(group)
            listOfAggregationOperation.add(matchOnGroup)
        } else {
            val group = Aggregation.group(Aggregation.fields().and(Field.userId.name, Field.userId.name))
            listOfAggregationOperation.add(group)
        }
        return listOfAggregationOperation
    }

    private fun parseEvents2(event: Event, tz: ZoneId, did: Boolean): Criteria {

        var matches = mutableListOf<Criteria>()
        matches.addAll(parsePropertyFilters(event, tz))
        matches.add(Criteria.where(Field.eventName.fName).`is`(event.name))
        matches.add(Criteria.where("userId").exists(true))
        matches.add(parseDateFilter(event.dateFilter, tz))

        var fields = Aggregation.fields(Field.userId.name, Field.creationTime.name, Field.clientId.name)
        matches.forEach { criteria ->
            val name = criteria.key
            if (name != null) {
                fields = fields.and(name, name)
            }
        }

        return Criteria().andOperator(*matches.toTypedArray())
    }

    private fun parseUsers(criteria: Criteria): Aggregation {
        //val project = Aggregation.project(Aggregation.fields("_id"))
        val matchOps = Aggregation.match(criteria)
        val group = Aggregation.group(Aggregation.fields().and("_id", "_id"))
        return Aggregation.newAggregation(matchOps, group)
    }

    private fun parseEvents(events: List<Event>, tz: ZoneId, did: Boolean, eventPropertyMatch: Criteria?, geoCriteria: Criteria?, userCriteria: Criteria?): List<Aggregation> {

         if(events.isNotEmpty()) {
            return events.map { event ->
                val matches = if (did) listOfNotNull(eventPropertyMatch, geoCriteria, userCriteria).toMutableList() else mutableListOf()
                matches.addAll(parsePropertyFilters(event, tz))
                matches.add(Criteria.where(Field.eventName.fName).`is`(event.name))
                matches.add(Criteria.where("userId").exists(true))
                matches.add(parseDateFilter(event.dateFilter, tz))
                var fields = Aggregation.fields(Field.userId.name, Field.creationTime.name, Field.clientId.name)
                matches.forEach { criteria ->
                    val name = criteria.key
                    if (name != null) {
                        fields = fields.and(name, name)
                    }
                }
//            val project = Aggregation.project(fields)
//                    .and("clientTime.month").`as`(Field.month.name)
//                    .and("clientTime.dayOfMonth").`as`(Field.monthday.name)
//                    .and("clientTime.dayOfWeek").`as`(Field.weekday.name)
//                    .and("clientTime.hour").`as`(Field.hour.name)
//                    .and("clientTime.minute").`as`(Field.minute.name)
//                    .and("clientTime.year").`as`(Field.year.name)


                val matchOps = Aggregation.match(Criteria().andOperator(*matches.toTypedArray()))
                val whereCond = if (did) {
                    event.whereFilter?.let { whereFilter -> whereFilterParse(whereFilter, tz) } ?: Optional.empty()
                } else Optional.empty()

            if (whereCond.isPresent) {
                val group = whereCond.get().first
                val matchOnGroup = whereCond.get().second
                Aggregation.newAggregation(/*project,*/ matchOps, group, matchOnGroup)
            } else {
                val group = Aggregation.group(Aggregation.fields().and(Field.userId.name, Field.userId.name))
                Aggregation.newAggregation(/*project,*/ matchOps, group)
            }



            }
        }else if(eventPropertyMatch !=null)  {
             var stage= mutableListOf<AggregationOperation>()
             stage.add(Aggregation.match(eventPropertyMatch))
             if(geoCriteria !=null){
                 stage.add(Aggregation.match(geoCriteria))
             }
             stage.add(Aggregation.project().and("userId").`as`("_id"))
             return listOf<Aggregation>(Aggregation.newAggregation(*stage.toTypedArray()))
         }
        else return emptyList()

    }

    fun parsePropertyFilters(event: Event, tz: ZoneId): List<Criteria> = event.propertyFilters.groupBy { it.name }.map { eventPropertyQuery(it.value, tz) }

    private fun eventPropertyQuery(eventProperties: List<PropertyFilter>, tz: ZoneId): Criteria {
        fun parseProperty(propertyFilter: PropertyFilter): Criteria {
            return when (propertyFilter.filterType) {
                PropertyFilterType.genericproperty -> {
                    val values = propertyFilter.values
                    when (propertyFilter.name) {
                        genericProperty.TimeOfDay.desc -> {
                            val (startHour, startMinute, endHour, endMinute) = values
                            Criteria().orOperator(
                                    Criteria(Field.hour.name).gte(startHour).lte(endHour),
                                    Criteria().andOperator(Criteria(Field.hour.name).`is`(startHour), Criteria(Field.minute.name).gt(startMinute)),
                                    Criteria().andOperator(Criteria(Field.hour.name).`is`(endHour), Criteria(Field.minute.name).lt(endMinute))
                            )
                        }
                        genericProperty.FirstTime.desc -> {
                            Criteria.where(Field.firstTime.name).`is`(true)
                        }
                        genericProperty.DayOfWeek.desc -> {
                            Criteria.where(Field.weekday.name).`in`(values)
                        }
                        genericProperty.DayOfMonth.desc -> {
                            Criteria.where(Field.month.name).`in`(values)
                        }
                        else -> throw Exception("{Invalid generic Property ${propertyFilter.name}}")
                    }
                }
                PropertyFilterType.UTM -> {
                    //FIXME UTM are like generic property only but have special behaviour
                    when (propertyFilter.name) {
                        utmProperty.UTMSource.desc -> Criteria()//listOf("attributes.${propertyFilter.name}", propertyFilter.operator, propertyFilter.values).joinToString(space)
                        utmProperty.UTMVisited.desc -> Criteria()//listOf("attributes.${propertyFilter.name}", propertyFilter.operator, propertyFilter.values).joinToString(space)
                        else -> throw Exception("Invalid UTM Property ${propertyFilter.name}")
                    }
                }
                PropertyFilterType.eventproperty -> {

                    match(propertyFilter.values, propertyFilter.operator, "attributes.${propertyFilter.name}", propertyFilter.type, propertyFilter.valueUnit, tz)
                }
                else -> {
                    throw Exception("type of filter can be eventptoperty, genericproperty or UTM but is null")
                }
            }
        }

        val rs = eventProperties.map {
            parseProperty(it)
        }
        return if (rs.size > 1) {
            Criteria().orOperator(*rs.toTypedArray())
        } else rs.first()

    }

    fun parseDateFilter(dateFilters: DateFilter, tz: ZoneId): Criteria = match(dateFilters.values, dateFilters.operator.name, Field.creationTime.name, DataType.date, dateFilters.valueUnit, tz)


    private fun whereFilterParse(whereFilter: WhereFilter, tz: ZoneId): Optional<Pair<GroupOperation, MatchOperation>> {
        val values = whereFilter.values
        return if ((values != null && values.isNotEmpty())) {
            val filter = when {
                whereFilter.whereFilterName == WhereFilterName.Count -> {

                    val group = Aggregation.group(Aggregation.fields().and(Field.userId.name, Field.userId.name)).count().`as`(Field.count.name)
                    val match = matchNumber(values.map { it.toString() }, whereFilter.operator, Field.count.name)

                    Pair(group, Aggregation.match(match))
                }
                whereFilter.whereFilterName == WhereFilterName.SumOfValuesOf && !whereFilter.propertyName.isNullOrBlank() -> {
                    val group = Aggregation.group(Aggregation.fields().and(Field.userId.name, Field.userId.name)).sum(whereFilter.propertyName).`as`(Field.sumof.name)
                    val match = matchNumber(values.map { it.toString() }, whereFilter.operator, Field.sumof.name)
                    Pair(group, Aggregation.match(match))

                }
                whereFilter.whereFilterName == WhereFilterName.SumOfValuesOf && whereFilter.propertyName.isNullOrBlank() -> {
                    throw Exception("invalid aggregate expression  ${whereFilter.whereFilterName} should have property specified")

                }
                else -> throw Exception("invalid aggregate expression can only be count or sum  but is ${whereFilter.whereFilterName}")
            }
            Optional.of(filter)
        } else Optional.empty()


    }


    private fun match(values: List<String>, operator: String, fieldName: String, type: DataType, unit: Unit, tz: ZoneId): Criteria {
        logger.debug("type : $type, operator: $operator and fieldname : $fieldName")
        return when (type) {
            DataType.string -> matchString(values, StringOperator.valueOf(operator), fieldName)
            DataType.number -> matchNumber(values, NumberOperator.valueOf(operator), fieldName)
            DataType.date -> matchDate(values, DateOperator.valueOf(operator), unit, fieldName, tz)
            DataType.range -> Criteria()
            DataType.boolean -> Criteria()

        }

    }

    private fun matchString(values: List<String>, operator: StringOperator, fieldName: String): Criteria {
        return when (operator) {
            StringOperator.Equals -> {
                Criteria.where(fieldName).`is`(values.first())
            }

            StringOperator.NotEquals -> {
                Criteria.where(fieldName).ne(values.first())
            }
            StringOperator.Contains -> {
                Criteria.where(fieldName).`in`(values)
            }
            StringOperator.DoesNotContain -> {
                Criteria.where(fieldName).nin(values)
            }

            StringOperator.Exists -> {
                Criteria.where(fieldName).exists(true)
            }
            StringOperator.DoesNotExist -> {
                Criteria.where(fieldName).exists(false)
            }
            else -> Criteria()

        }

    }


    private fun matchNumber(valuesString: List<String>, operator: NumberOperator, fieldName: String): Criteria {
        val values = valuesString.map { it.toLong() }
        return when (operator) {
            NumberOperator.Equals -> {
                Criteria.where(fieldName).`is`(values.first())
            }
            NumberOperator.Between -> {
                Criteria.where(fieldName).gt(values.first()).lt(values.last())
            }
            NumberOperator.GreaterThan -> {
                Criteria.where(fieldName).gt(values.first())
            }
            NumberOperator.LessThan -> {
                Criteria.where(fieldName).gt(values.first())
            }
            NumberOperator.NotEquals -> {
                Criteria.where(fieldName).ne(values.first())
            }

            NumberOperator.Exists -> {
                Criteria.where(fieldName).exists(true)
            }
            NumberOperator.DoesNotExist -> {
                Criteria.where(fieldName).exists(false)
            }
            else -> Criteria()
        }

    }

    private fun matchDate(values: List<String>, operator: DateOperator, unit: Unit, fieldName: String, tz: ZoneId): Criteria {

        return when (operator) {
        //absolute comparison starts
            DateOperator.Before -> {

                val start = dateUtils.getStartOfDay(values.first(), tz)
                Criteria.where(fieldName).lte(start)
            }
            DateOperator.After -> {
                val end = dateUtils.getMidnight(values.first(), tz)
                Criteria.where(fieldName).gte(end)
            }
            DateOperator.AfterTime -> {
                val end = dateUtils.parseDateTime(values.first())
                Criteria.where(fieldName).gte(end)
            }
            DateOperator.On -> {
                val start = dateUtils.getStartOfDay(values.first(), tz)
                val end = dateUtils.getMidnight(values.first(), tz)
                Criteria.where(fieldName).lte(end).gte(start)
            }
            DateOperator.Between -> {
                var startDate = dateUtils.getStartOfDay(values.first(), tz)
                var endDate = dateUtils.getMidnight(values.last(), tz)
                Criteria.where(fieldName).gte(startDate).lte(endDate)
            }
            DateOperator.BetweenTime -> {
                var startDateTime = dateUtils.parseDateTime(values.first())
                var endDateTime = dateUtils.parseDateTime(values.last())
                Criteria.where(fieldName).gte(startDateTime).lte(endDateTime)
            }
        //absolute comparison ends
        //relative comparison starts
            DateOperator.InThePast -> {

                val startLocalDateTime = minus(LocalDateTime.now(tz), unit, values.first().toLong())
                val startTzDateTime = Date.from(startLocalDateTime.atZone(tz).toInstant())
                val endTzDateTime = Date.from(LocalDateTime.now(tz).atZone(tz).toInstant())

                Criteria.where(fieldName).gte(startTzDateTime).lte(endTzDateTime)

            }
            DateOperator.WasExactly -> {
                val startLocalDate = LocalDate.now(tz).minusDays(values.first().toLong())
                val startLocalDateTime = startLocalDate.atStartOfDay()
                val endLocalDateTime = startLocalDate.plusDays(1).atStartOfDay()
                val startTzDateTime = Date.from(startLocalDateTime.atZone(tz).toInstant())
                val endTzDateTime = Date.from(endLocalDateTime.atZone(tz).toInstant())
                Criteria.where(fieldName).lte(endTzDateTime).gte(startTzDateTime)
            }
            DateOperator.Today -> {
                val startLocalDate = LocalDate.now(tz)
                val startLocalDateTime = startLocalDate.atStartOfDay()
                val endLocalDateTime = startLocalDate.plusDays(1).atStartOfDay()
                val startTzDateTime = Date.from(startLocalDateTime.atZone(tz).toInstant())
                val endTzDateTime = Date.from(endLocalDateTime.atZone(tz).toInstant())
                Criteria.where(fieldName).lte(endTzDateTime).gte(startTzDateTime)
            }
            DateOperator.InTheFuture -> {

                val endLocalDateTime = plus(LocalDateTime.now(tz), unit, values.first().toLong())
                val endTzDateTime = Date.from(endLocalDateTime.atZone(tz).toInstant())
                val startTzDateTime = Date.from(LocalDateTime.now(tz).atZone(tz).toInstant())

                Criteria.where(fieldName).gte(startTzDateTime).lte(endTzDateTime)

            }
            DateOperator.WillBeExactly -> {
                //1 is aadded to make day go to moprning of next day 00:00 hours for lte comparision
                val endLocalDate = LocalDate.now(tz).plusDays(values.first().toLong())
                val endLocalDateTime = endLocalDate.plusDays(1).atStartOfDay()
                val startLocalDateTime = LocalDate.now(tz).atStartOfDay()
                val startTzDateTime = Date.from(startLocalDateTime.atZone(tz).toInstant())
                val endTzDateTime = Date.from(endLocalDateTime.atZone(tz).toInstant())
                Criteria.where(fieldName).lte(endTzDateTime).gte(startTzDateTime)

            }
            DateOperator.Exists -> {
                Criteria.where(fieldName).exists(true)
            }
            DateOperator.DoesNotExist -> {
                Criteria.where(fieldName).exists(false)
            }
            else -> Criteria()

        }

    }

    private fun minus(date: LocalDateTime, unit: Unit, value: Long): LocalDateTime {

        return when (unit) {
            Unit.mins -> date.minusMinutes(value)
            Unit.hours -> date.minusHours(value)
            Unit.days -> date.minusDays(value)
            Unit.week -> date.minusWeeks(value)
            Unit.month -> date.minusMonths(value)
            Unit.year -> date.minusYears(value)
            else -> date
        }
    }

    private fun plus(date: LocalDateTime, unit: Unit, value: Long): LocalDateTime {
        return when (unit) {
            Unit.mins -> date.plusMinutes(value)
            Unit.hours -> date.plusHours(value)
            Unit.days -> date.plusDays(value)
            Unit.week -> date.plusWeeks(value)
            Unit.month -> date.plusMonths(value)
            Unit.year -> date.plusYears(value)
            else -> date
        }
    }


    fun getFieldPath(filterType:GlobalFilterType,name:String):String{
        when(filterType){
            GlobalFilterType.Demographics->return "standardInfo.${name}"
            GlobalFilterType.UserProperties-> return "additionalInfo.${name}"
            GlobalFilterType.Reachability->return  "communication.${name}.dnd"
            GlobalFilterType.UserComputedProperties-> return "${name}"
            GlobalFilterType.UserIdentity->return "identity.${name}"
            GlobalFilterType.UserTechnographics->return return "system.${name}.name"
            GlobalFilterType.AppFields->return return "appfield.${name}.name"

            GlobalFilterType.Geogrophy->return "geogrophy.${name}"
            GlobalFilterType.Technographics->return "system.${name}.name"
            GlobalFilterType.EventProperties->return "${name}"
            GlobalFilterType.EventAttributeProperties-> return "attributes.${name}"
            GlobalFilterType.EventTimeProperties-> return "clientTime.${name}"
            GlobalFilterType.EventComputedProperties->return "${name}"
            else-> return ""
        }
    }

    private fun isUserCollection(globalFilterType: GlobalFilterType): Boolean {
        //TODO, move it to the enum so in case of a new entry in enum it doesn't get missed
        return globalFilterType in listOf(GlobalFilterType.UserProperties, GlobalFilterType.Demographics, GlobalFilterType.Reachability, GlobalFilterType.UserComputedProperties)
    }


    fun joinAwareFilterGlobalQ(globalFilters: List<GlobalFilter>, tz: ZoneId, userId: String?, joinWithUser: Boolean): Pair<Criteria?, Criteria?>{
        fun parseGlobalFilter(filter: GlobalFilter,filterType:GlobalFilterType): Criteria {
            var fieldPath= getFieldPath(filterType,filter.name)
            val fieldName = if(joinWithUser &&  isUserCollection(filterType)) "$USER_DOC.$fieldPath" else "$fieldPath"
            var values = filter.values
            if(fieldPath.equals("standardInfo.age")){
                var v:MutableList<String> = mutableListOf()
                v.add(0,(LocalDateTime.now().year - Integer.parseInt(filter.values[1])).toString())
                v.add(1,(LocalDateTime.now().year - Integer.parseInt(filter.values[0])).toString())
                values=v
            }
            val type = filter.type
            val unit = filter.valueUnit

            val operator = filter.operator
            return match(values, operator, fieldName, type, unit, tz)
        }

        fun parse(filters: Map<String, List<GlobalFilter>>): Criteria {

            val criteriaList = filters.map { filterList ->
                val sameNameCriteria = filterList.value.map { filter ->
                    var filterType = filter.globalFilterType
                    parseGlobalFilter(filter, filterType)

                }
                if (sameNameCriteria.size == 1) sameNameCriteria.get(0) else Criteria().orOperator(*sameNameCriteria.toTypedArray())
            }

            return if (criteriaList.size == 1) criteriaList.get(0) else Criteria().andOperator(*criteriaList.toTypedArray())
        }

        val eventPropertyMatchCriteria = mutableListOf<Criteria>()
        val userPropertyMatchCriteria = mutableListOf<Criteria>()

        globalFilters
                .groupBy { globalFilter -> globalFilter.globalFilterType }
                .forEach { gFilterType, gFilterList ->
                    val filter = gFilterList.groupBy { it.name }
                    val criteria = parse(filter)
                    when (gFilterType) {
                    //GlobalFilterType.AppFields -> eventPropertyMatchCriteria.add(criteria)
                        GlobalFilterType.Demographics -> userPropertyMatchCriteria.add(criteria)
                        GlobalFilterType.Reachability -> userPropertyMatchCriteria.add(criteria)
                        GlobalFilterType.UserProperties -> userPropertyMatchCriteria.add(criteria)
                        GlobalFilterType.UserComputedProperties -> userPropertyMatchCriteria.add(criteria)
                        GlobalFilterType.UserIdentity -> userPropertyMatchCriteria.add(criteria)
                        GlobalFilterType.UserTechnographics -> userPropertyMatchCriteria.add(criteria)

                        GlobalFilterType.EventProperties -> eventPropertyMatchCriteria.add(criteria)
                        GlobalFilterType.EventAttributeProperties -> eventPropertyMatchCriteria.add(criteria)
                        GlobalFilterType.EventComputedProperties -> eventPropertyMatchCriteria.add(criteria)
                        GlobalFilterType.EventTimeProperties -> eventPropertyMatchCriteria.add(criteria)
                        GlobalFilterType.Technographics -> eventPropertyMatchCriteria.add(criteria)
                        GlobalFilterType.Geogrophy -> eventPropertyMatchCriteria.add(criteria)
                    }
                }

        if (userId != null) {
            if (eventPropertyMatchCriteria.isNotEmpty()) eventPropertyMatchCriteria.add(Criteria.where("userId").`is`(userId))
            if (userPropertyMatchCriteria.isNotEmpty()) userPropertyMatchCriteria.add(Criteria.where("id").`is`(userId))
        }

        val eventCriteria = if (eventPropertyMatchCriteria.size == 1) eventPropertyMatchCriteria.get(0)
        else (if (eventPropertyMatchCriteria.isNotEmpty()) Criteria().andOperator(*eventPropertyMatchCriteria.toTypedArray()) else null)
        //val eventMatch = Aggregation.match(eventCriteria)

        val userCriteria = if (userPropertyMatchCriteria.size == 1) userPropertyMatchCriteria.get(0)
        else (if (userPropertyMatchCriteria.isNotEmpty()) Criteria().andOperator(*userPropertyMatchCriteria.toTypedArray()) else null)
        //val userMatch = Aggregation.match(userCriteria)

        return Pair(eventCriteria, userCriteria)
    }

    fun filterGlobalQ(globalFilters: List<GlobalFilter>, tz: ZoneId): Pair<Criteria?, Criteria?> {
        return joinAwareFilterGlobalQ(globalFilters, tz, null, false)
    }

    private fun filterGlobalQWithUserId(globalFilters: List<GlobalFilter>, tz: ZoneId, userId: String?): Pair<Criteria?, Criteria?> {
        return joinAwareFilterGlobalQ(globalFilters, tz, userId, false)
    }


    private fun filterGeography(geofilter: List<Geography>, userId: String?): Criteria {

        val criteria = geofilter.map { geo ->
            val country = geo.country?.name?.let { name -> Criteria.where("geogrophy.country").`is`(name) }
            val state = geo.state?.name?.let { name -> Criteria.where("geogrophy.state").`is`(name) }
            val city = geo.city?.name?.let { name -> Criteria.where("geogrophy.city").`is`(name) }
            var geoCriteria = listOfNotNull(country, state, city)
            if (geoCriteria.isNotEmpty()) {
                if (userId != null) {
                    geoCriteria = listOfNotNull(Criteria.where("userId").`is`(userId), *geoCriteria.toTypedArray())
                }
                Criteria().andOperator(*geoCriteria.toTypedArray())
            } else null


        }.filterNotNull()

        return Criteria().orOperator(*criteria.toTypedArray())
    }


}


class SegmentQuery(
        val didq: Pair<List<Aggregation>, ConditionType>,
        val didntq: Pair<List<Aggregation>, ConditionType>,
        val userQuery: Aggregation?,
        val query: Criteria? = null) : Serializable


