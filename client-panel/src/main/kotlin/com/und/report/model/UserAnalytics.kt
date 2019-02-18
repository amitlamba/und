package com.und.report.model

import com.und.web.model.GlobalFilterType
import org.springframework.data.annotation.Id
import sun.awt.EventListenerAggregate
import java.io.Serializable
import java.time.LocalDate
import java.util.*
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

data class UserCountForProperty(var usercount: Int, var propertyName: String)

data class UserCountForTime(var usercount: Int, var time: Int)

data class UserCountTrendForDate(var date: String, var trenddata: List<UserCountForTime>)

data class UserCountByTypeForTime(var newusercount: Int, var oldusercount: Int, var time: Int)

data class UserTypeTrendForDate(var date: String, var userCountData: List<UserCountByTypeForTime>)

data class UserCountByEvent(var usercount: Int, var eventname: String)

data class UserCountByEventForDate(var date: String, var userCountData: List<UserCountByEvent>)

data class AggregateOutput(var groupByInfo: Map<String, Any>, var aggregateVal: Double)

data class EventChronology(var Event: String = "", var attribute: String = "all", var chronology: List<Long> = emptyList())

data class UserData(@Id var userId: String = "", var chronologies: List<EventChronology> = emptyList())

data class FunnelData(var userData: List<UserData> = emptyList(),  var eventsOrder: List<String> = emptyList(), var maxInterval: Int = 0)

data class SegmentTrendCount(var date:String,var count:Int)

