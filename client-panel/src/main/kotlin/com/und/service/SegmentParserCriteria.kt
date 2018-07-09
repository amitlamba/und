package com.und.service

import com.und.common.utils.DateUtils
import com.und.common.utils.loggerFor
import com.und.web.model.*
import com.und.web.model.Unit
import org.slf4j.Logger
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.GroupOperation
import org.springframework.data.mongodb.core.aggregation.MatchOperation
import org.springframework.data.mongodb.core.query.Criteria
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

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
 */
class SegmentParserCriteria {

    companion object {
        val logger: Logger = loggerFor(SegmentParserCriteria::class.java)
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

        //did
        val did = segment.didEvents
        val didq =
                did?.let { Pair(parseEvents(it.events, tz, true), it.joinCondition.conditionType) }
                        ?: Pair(emptyList(), ConditionType.AllOf)


        //and not
        val didnot = segment.didNotEvents
        val didnotq = didnot?.let { Pair(parseEvents(it.events, tz, false), ConditionType.AnyOf) }
                ?: Pair(emptyList(), ConditionType.AnyOf)


        val gFilters = segment.globalFilters
        val matches = filterGlobalQ(gFilters, tz)






        return SegmentQuery(didq, didnotq)
    }


    fun parseEvents(events: List<Event>, tz: ZoneId, did: Boolean): List<Aggregation> {

        return events.map { event ->
            val matches = mutableListOf<Criteria>()
            matches.addAll(parsePropertyFilters(event,  tz))
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
            val project = Aggregation.project(fields)
                    .and("clientTime.month").`as`(Field.month.name)
                    .and("clientTime.dayOfMonth").`as`(Field.monthday.name)
                    .and("clientTime.dayOfWeek").`as`(Field.weekday.name)
                    .and("clientTime.hour").`as`(Field.hour.name)
                    .and("clientTime.minute").`as`(Field.minute.name)
                    .and("clientTime.year").`as`(Field.year.name)


            val matchOps = Aggregation.match(Criteria().andOperator(*matches.toTypedArray()))

            val whereCond = if (did) {
                event.whereFilter?.let { whereFilter -> whereFilterParse(whereFilter, tz) } ?: Optional.empty()
            } else Optional.empty()

            if (whereCond.isPresent) {
                val group = whereCond.get().first
                val matchOnGroup = whereCond.get().second
                Aggregation.newAggregation(project, matchOps, group, matchOnGroup)
            } else {
                val group = Aggregation.group(Aggregation.fields().and(Field.userId.name, Field.userId.name))
                Aggregation.newAggregation(project, matchOps, group)
            }


        }

    }

    private fun parsePropertyFilters(event: Event,  tz: ZoneId): List<Criteria> = event.propertyFilters.groupBy { it.name }.map { eventPropertyQuery(it.value, tz) }


    private fun eventPropertyQuery(eventProperties: List<PropertyFilter>,  tz: ZoneId): Criteria {
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

    private fun parseDateFilter(dateFilters: DateFilter, tz: ZoneId): Criteria = match(dateFilters.values, dateFilters.operator.name, Field.creationTime.name, DataType.date, dateFilters.valueUnit, tz)


    private fun whereFilterParse(whereFilter: WhereFilter,  tz: ZoneId): Optional<Pair<GroupOperation, MatchOperation>> {
        val values = whereFilter.values
        return if ((values != null && values.isNotEmpty()) && !whereFilter.propertyName.isNullOrBlank()) {
            val filter: Pair<GroupOperation, MatchOperation> = when (whereFilter.whereFilterName) {
                WhereFilterName.Count -> {

                    val group = Aggregation.group(Aggregation.fields().and(Field.userId.name, Field.userId.name)).count().`as`(Field.count.name)
                    val match = matchNumber(values.map { it.toString() }, whereFilter.operator, Field.count.name)
                    Pair(group, Aggregation.match(match))
                }
                WhereFilterName.SumOfValuesOf -> {

                    val group = Aggregation.group(Aggregation.fields().and(Field.userId.name, Field.userId.name)).sum(whereFilter.propertyName).`as`(Field.sumof.name)
                    val match = matchNumber(values.map { it.toString() }, whereFilter.operator, Field.sumof.name)

                    Pair(group, Aggregation.match(match))
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

    private fun filterGlobalQ(globalFilters: List<GlobalFilter>, tz: ZoneId): Pair<MatchOperation, MatchOperation>? {
        fun parseGlobalFilter(filter: GlobalFilter): Criteria {
            val fieldName = filter.name
            val type = filter.type
            val unit = filter.valueUnit
            val values = filter.values
            val operator = filter.operator
            return match(values, operator, fieldName, type, unit, tz)
        }

        fun parse(filters: Map<String, List<GlobalFilter>>): Criteria {

            val criteriaList = filters.map { filterList ->
                val criteriaList = filterList.value.map { filter ->
                    parseGlobalFilter(filter)

                }
                Criteria().orOperator(*criteriaList.toTypedArray())
            }

            return Criteria().andOperator(*criteriaList.toTypedArray())
        }

        val gFilters = globalFilters
                .groupBy { it.globalFilterType }

        val f = Pair(mutableListOf<Criteria>(), mutableListOf<Criteria>())
        gFilters.forEach { gFilterType, gFilterList ->
            val filter = gFilterList.groupBy { it.name }
            val criteria = parse(filter)
            when (gFilterType) {
                GlobalFilterType.AppFields -> f.first.add(criteria)

                GlobalFilterType.Demographics -> f.second.add(criteria)
                GlobalFilterType.Reachability -> f.second.add(criteria)
                GlobalFilterType.Technographics -> f.first.add(criteria)
                GlobalFilterType.UserProperties -> f.second.add(criteria)
            }
        }

        val eventCriteria = Criteria().andOperator(*f.first.toTypedArray())
        val eventMatch = Aggregation.match(eventCriteria)

        val userCriteria = Criteria().andOperator(*f.second.toTypedArray())
        val userMatch = Aggregation.match(userCriteria)

        return Pair(eventMatch, userMatch)


    }


}


class SegmentQuery(val didq: Pair<List<Aggregation>, ConditionType>, val didntq: Pair<List<Aggregation>, ConditionType>)