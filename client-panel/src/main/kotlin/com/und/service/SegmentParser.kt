package com.und.service

import com.und.web.model.*

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
class SegmentParser {
    val lt = "\$lt"
    val gt = "\$gt"
    val eq = "\$eq"
    val neq = "\$neq"
    val lte = "\$lte"
    val gte = "\$gte"
    val inarray = "\$in"
    val nin = "\$nin"
    val subtract = "\$subtract"
    val add = "\$add"
    val today = "new ISODate"
    val exists = "\$exists"
    val q_match = "\$match"
    val q_addFields = "\$addFields"
    val q_groups = "\$group"

    val dayOfWeek = "\$dayOfWeek"
    val dayOfMonth = "\$dayOfMonth"
    val month = "\$month"
    val hour = "\$hour"
    val minute = "\$minute"
    val second = "\$second"
    val year = "\$year"
    val creationTime = "\$creationTime"
    val userId = "\$userId"
    val sum = "\$sum"
    val clientId = "\$clientId"

    private val newLine = "\n"
    private val and = "\$and "
    private val or = "\$or "
    private val space = " "

    private val query = """

    """.trimIndent()

    fun segmentQueries(segment: Segment): SegmentQ {
        fun parseCondition(conditionType: ConditionType) =
                when (conditionType) {
                    ConditionType.AllOf -> and
                    ConditionType.AnyOf -> or
                }

        //did
        val did = segment.didEvents
        val didq = if (did != null) {
            val didq = did.let { if (it.events.isNotEmpty()) parseEvents(it.events, true) else null }
            val condition = parseCondition(did.joinCondition.conditionType)
            didq?: emptyList()
        } else emptyList()

        //and not
        val didnot = segment.didNotEvents
        val didnotq = if (didnot != null) {
            didnot.let { if (it.events.isNotEmpty()) parseEvents(it.events, false) else null }?: emptyList()
        } else emptyList()





        return SegmentQ(didq,didnotq)
    }


    fun parseEvents(events: List<Event>, did: Boolean): List<String> = events.map {
        val whereCond = it.whereFilter?.let { whereFilterParse(it) }
        val matches = parsePropertyFilters(it)
        matches.plus("{name:${it.name}}")
        matches.plus(parseDateFilter(it.dateFilter))
        """

                        {
                            $q_addFields: {
                                weekday: {$dayOfWeek: "$creationTime"},
                                monthday: {$dayOfMonth: "$creationTime"},
                                month: {$month: "$creationTime"},
                                hour: {$hour: "$creationTime"},
                                minute: {$minute: "$creationTime"},
                                second: {$second: "$creationTime"},
                                year: {$year: "$creationTime"}
                            }
                        },
                        {
                            $q_match:
                             {
                                ${matches.joinToString(",$newLine")}
                             }
                         },
                         {
                            $q_groups:{
                                _id: "$userId"
                                ${if (did) ",${whereCond?.first}" else ""}

                         }
                     },
                     {
                         $q_match: {
                          ${if (did) "${whereCond?.second}" else ""}
                         }
                     }
            """

    }


    private fun parsePropertyFilters(event: Event): List<String> {
        return event.propertyFilters.groupBy { it.name }.map {
            eventPropertyQuery(it.value)
        }.filter({ it -> it.isNotBlank() })

    }


    private fun eventPropertyQuery(eventProperties: List<PropertyFilter>): String {
        fun parseProperty(propertyFilter: PropertyFilter): String {
            return when (propertyFilter.filterType) {
                PropertyFilterType.eventproperty -> {

                    match(propertyFilter.values, propertyFilter.operator, "\"attributes.${propertyFilter.name}\"", propertyFilter.type, propertyFilter.valueUnit.name)
                }
                PropertyFilterType.genericproperty -> {
                    val values = propertyFilter.values
                    when (propertyFilter.name) {
                        "Time of day" -> {
                            val (startHour, startMinute, endHour, endMinute) = values

                            """{$or:[hour:{$gt:$startHour, $lt:$endHour},
                                    |       $and:[hour:{$eq:startHour}, minute:{$gt:startMinute}],
                                    |       $and:[hour:{$eq:endHour}, minute:{$lt:startMinute}]
                                    ]
                                }
                            """.trimMargin()
                        }
                        "First Time" -> {
                            //FIXME how to do this?
                            "{count of event == 1}"
                        }
                        "Day of Week" -> {
                            "{weekday:{$inarray:[${values.joinToString(",")}]}}"
                        }
                        "Day Of Month" -> {
                            "{month:{$inarray:[${values.joinToString(",")}]}}"
                        }
                        else -> "{Invalid generic Property ${propertyFilter.name}}"
                    }


                }
                PropertyFilterType.UTM -> {
                    //FIXME UTM are like generic property only but have special behaviour
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
        val finalq = if (rs.size > 1) {
            "$or:[${rs.map { "{$it}" }.joinToString(" , $newLine")}]"
        } else rs.first()
        //joined with or for same name properties of same event
        return finalq
    }

    private fun parseDateFilter(dateFilters: DateFilter): String {

        return match(dateFilters.values, dateFilters.operator.name, "creationTime", DataType.date, dateFilters.valueUnit.name)

    }


    private fun whereFilterParse(whereFilter: WhereFilter): Pair<String, String> {
        val (groupOps, fieldName) = when (whereFilter.whereFilterName) {
            WhereFilterName.Count -> {
                Pair("count:{$sum:1}", "count")
            }
            WhereFilterName.SumOfValuesOf -> {
                Pair("sumof:{$sum:\"\$${whereFilter.propertyName}\"}", "sumof")
            }
            else -> throw Exception("invalid aggregate expression can only be count or sum  but is ${whereFilter.whereFilterName}")
        }

        val values = whereFilter.values
        return if (values != null) {
            Pair(groupOps, matchNumber(values.map { it.toString() }, whereFilter.operator?.name ?: "Equals", fieldName))
        } else Pair("", "")
    }


    private fun match(values: List<String>, operator: String, fieldName: String, type: DataType, unit: String): String {
        return when (type) {
            DataType.string -> matchString(values = values, operator = operator, fieldName = fieldName)
            DataType.number -> matchNumber(values = values, operator = operator, fieldName = fieldName)
            DataType.date -> matchDate(values = values, operator = operator, fieldName = fieldName, unit = unit)
            else -> "{}"

        }

    }

    private fun matchString(values: List<String>, operator: String, fieldName: String): String {
        return when (operator) {
            "Equals" -> "$fieldName:\"${values.first()}\""

            "NotEquals" -> "$fieldName:{ $neq:\"${values.last()}}\""
            "Contains" -> "$fieldName:{ $inarray:[\"${values.joinToString("\" , \"")}\"]}"
            "DoesNotContain" -> "$fieldName:{ $nin:[\"${values.joinToString("\" , \"")}\"]}"

            "Exists" -> "$fieldName:{$exists:true}"
            "DoesNotExist" -> "$fieldName:{$exists:false}"

            else -> space
        }

    }


    private fun matchNumber(values: List<String>, operator: String, fieldName: String): String {
        return when (operator) {
            "Equals" -> "$fieldName:${values.first()}"
            "Between" -> "$fieldName:{$gt:${values.first()}, $lt:${values.last()}}"
            "GreaterThan" -> "$fieldName:{$gt:${values.first()}}}"
            "LessThan" -> "$fieldName:{ $lt:${values.last()}}"
            "NotEquals" -> "$fieldName:{ $neq:${values.last()}}"

            "Exists" -> "$fieldName:{$exists:true}"
            "DoesNotExist" -> "$fieldName:{$exists:false}"

            else -> space
        }

    }

    private fun matchDate(values: List<String>, operator: String, unit: String, fieldName: String): String {

        return when (operator) {

            "Before" -> {
                val date = values.first()
                "$fieldName:{$lt:new ISODate($date)}"
            }
            "After" -> {
                val date = values.first()
                "$fieldName:{$gt:new ISODate($date)}"
            }
            "On" -> {
                val date = values.first()
                "$fieldName:{$eq:new ISODate($date)}"
            }
            "Between" -> {
                val startDate = "new ISODate(${values.first()})"
                val endDate = "new ISODate(${values.last()})"
                "$fieldName:{$lte:$endDate, $gte:new $startDate}"
            }
            "InThePast" -> {
                val ms: Long = msIn(unit)
                val (start, end) = if (values.size == 2)
                    Pair(values.first().toLong() * ms, values.last().toLong() * ms)
                else
                    Pair(0L, values.first().toString().toLong() * ms)
                val startDate = "dateDifference: { $subtract: [ $today,$start ] }"
                val endDate = "dateDifference: { $subtract: [ $today,$end ] }"

                "$fieldName:{$lte:$endDate, $gte:new $startDate}"

            }
            "WasExactly" -> {
                //FIXME overflow issue
                val diff = values.first().toLong() * msIn("day")
                val startDate = "dateDifference: { $subtract: [ $today,$diff ] }"
                "$fieldName:{$eq:$startDate}"
            }
            "Today" -> {
                "{creationTime: new ISODate()}"
            }
            "InTheFuture" -> {
                //FIXME this issue of dates
                val ms: Long = msIn(unit)
                val (start, end) = if (values.size == 2)
                    Pair(values.first().toLong() * ms, values.last().toLong() * ms)
                else
                    Pair(0L, values.first().toLong() * ms)
                val startDate = "dateDifference: { $add: [ $today,$start ] }"
                val endDate = "dateDifference: { $add: [ $today,$end ] }"

                "$fieldName:{$lte:$endDate, $gte:new $startDate}"
            }
            "WillBeExactly" -> {
                //FIXME overflow issue
                val diff = values.first().toLong() *  msIn("day")
                val startDate = "dateDifference: { $add: [ $today,$diff ] }"
                "$fieldName:{$eq:$startDate}"
            }
            "Exists" -> {
                "$fieldName:{$exists:true}"
            }
            "DoesNotExist" -> {
                "$fieldName:{$exists:false}"
            }
            else -> space


        }

    }

    private fun msIn(unit: String): Long {
        val ms: Long = when (unit) {
            "day" -> 24L * 3600000L
            "week" -> 7 * 24 * 3600000L
            "month" -> 30L * 24L * 3600000L
            "year" -> 365L * 30L * 24L * 3600000L
            else -> 0L
        }
        return ms
    }

    private fun filterGlobalQOr(globalFilters: List<GlobalFilter>): String {


        fun parse(filter: Map<String, List<GlobalFilter>>): String {
            val q = filter.map {
                val glFilters = it.value
                "(" + glFilters.map { filter ->
                    val type = filter.type
                    val filterString = when (type) {
                        DataType.string -> "[" + filter.values.mapNotNull { it.toString() }.joinToString(",") + "]"
                        DataType.number -> "[" + filter.values.mapNotNull { it.toString() }.joinToString(",") + "]"
                        DataType.date -> "[" + filter.values.mapNotNull { it.toString() }.joinToString(",") + "]"
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
}

class SegmentQ(val didq:List<String>, val didntq: List<String>)