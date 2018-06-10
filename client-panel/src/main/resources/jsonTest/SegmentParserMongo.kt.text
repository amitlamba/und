package com.und.service

import com.und.model.mongo.eventapi.EventUser
import com.und.web.model.*
import java.time.LocalDateTime

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
class SegmentParserMongo {

    private val newLine = "\n"
    private val and = " and "
    private val or = " or "
    private val space = " "

    fun userList(segment: Segment): List<EventUser> {

        //did
        val did = segment.didEvents
        val didq = did?.let { if (it.events.isNotEmpty()) parseEvents(it) else null }

        //and not
        val didnot = segment.didNotEvents
        val didnotq = didnot?.let { if (it.events.isNotEmpty()) " ( not ${parseEvents(it)} )" else null }


        //and
        //for same properties use in/or
        val filterq = filterGlobalQOr(segment.globalFilters)


        //and
        val geoFilters = segment.geographyFilters

        val finalq = listOf(didq, didnotq, filterq).filterNotNull().joinToString(and)
        println(finalq)

        return emptyList()
    }

    private fun filterGlobalQOr(globalFilters: List<GlobalFilter>): String {


        fun parse(filter: Map<String, List<GlobalFilter>>): String {
            val q = filter.map {
                val glFilters = it.value
                "(" + glFilters.map { filter ->
                    val type = filter.type
                    val unit = filter.valueUnit
                    val filterString = when (type) {
                        "string" -> "[" + filter.values.mapNotNull { it.toString() }.joinToString(",") + "]"
                        "number" -> "[" + filter.values.mapNotNull { it.toString() }.joinToString(",") + "]"
                        "date" -> "[" + filter.values.mapNotNull { it.toString() }.joinToString(",") + "]"
                        else -> "[" + filter.values.mapNotNull { it.toString() }.joinToString(",") + "]"
                    }
                    "${filter.globalFilterType}.${filter.name} ${filter.operator} $filterString"
                }.joinToString(or) + ")$newLine"

            }.joinToString(and)
            return q
        }

        val gFilters = globalFilters
                .groupBy { it.globalFilterType }

        return gFilters.map { it ->
            val filter = it.value.groupBy { it.name }
            parse(filter)
        }.joinToString(and)
    }


    private fun parseEvents(did: DidEvents): String {

         fun parseEvents(events: List<Event>, conditionType: ConditionType): String {
            fun parseCondition(conditionType: ConditionType) =
                    when (conditionType) {
                        ConditionType.AllOf -> and
                        ConditionType.AnyOf -> or
                    }

            val condition = parseCondition(conditionType)
            val eventq = events.map {
                val q = listOf("name==${it.name}", parsePropertyFilters(it), parseDateFilter(it.dateFilter), it.whereFilter?.let { whereFilterParse(it) })
                        .filterNotNull()
                        .joinToString(" $and $newLine")
                "(where $q)"
            }

            return eventq.joinToString(condition)
        }
        return parseEvents(did.events, did.joinCondition.conditionType)
    }


    private fun parsePropertyFilters(event: Event): String =
            event.propertyFilters.groupBy { it.name }.map {
                val name: String = it.key
                val eventProperties: List<PropertyFilter> = it.value

                val eventPropertyQuery = eventPropertyQuery(eventProperties)

                return "( $eventPropertyQuery )"


            }.joinToString(and)

    private fun eventPropertyQuery(eventProperties: List<PropertyFilter>): String {
        /*            val propertName = it.name
                    val operator = it.operator
                    val type = it.type
                    val valueUnit = it.valueUnit
                    val values = it.values
                    val propertiesPart = " ${it.name} ${it.filterType} ${it.operator} ${it.type} ${it.valueUnit} ${it.values}"*/
        fun parseProperty(propertyFilter: PropertyFilter): String {
            return when (propertyFilter.filterType) {
                PropertyFilterType.eventproperty -> {
                    //event property are like amount etc that comes with event custom
                    //search in event attributes
                    //check for type of operator and make query accordingly
                    listOf("attributes.${propertyFilter.name}", propertyFilter.operator, propertyFilter.values).joinToString(space)
                }
                PropertyFilterType.genericproperty -> {
                    // these are like firstTime, lastTime, time of day, day of week, day of month etc.
                    //query them using time of event, e.g. for lastTime? firstTime
                    //firstTime when a user has done event with this name and no more after(count should be one)
                    //day of week is when occurrence is at that day of week
                    when (propertyFilter.name) {
                        "Time of day" -> {
                            "creationTime.time between propertyFilter.values"
                        }
                        "First Time" -> {
                            "count of event == 1"
                        }
                        "Day of Week" -> {
                            "creationTime.week equals propertyFilter.values"
                        }
                        "Day Of Month" -> {
                            "creationTime.date equals propertyFilter.values"
                        }
                        else -> "Invalid generic Property ${propertyFilter.name}"
                    }


                }
                PropertyFilterType.UTM -> {
                    //UTM are like generic property only but have special behaviour
                    when (propertyFilter.name) {
                        "UTM Source" -> listOf("attributes.${propertyFilter.name}", propertyFilter.operator, propertyFilter.values).joinToString(space)
                        "UTM Visited" -> listOf("attributes.${propertyFilter.name}", propertyFilter.operator, propertyFilter.values).joinToString(space)
                        else -> "Invalid UTM Property ${propertyFilter.name}"
                    }
                }
                else -> {
                    throw Exception("type of filter can be eventptoperty, genericproperty or UTM but is null")
                }
            }
        }

        val rs = eventProperties.map {
            parseProperty(it)
        }
        return rs.joinToString(or)
    }


    private fun parseDateFilter(dateFilters: DateFilter): String {
        return listOf("creationTime", dateFilters.operator, dateFilters.values).joinToString(space)

    }

    private fun whereFilterParse(whereFilter: WhereFilter): String {
        return when (whereFilter.whereFilterName) {
            WhereFilterName.Count -> {
                listOf("count", whereFilter.operator, whereFilter.values).joinToString(space)
            }
            WhereFilterName.SumOfValuesOf -> {
                listOf("sumof(${whereFilter.propertyName})", whereFilter.operator, whereFilter.values).joinToString(space)
            }
            else -> space
        }
    }
}

