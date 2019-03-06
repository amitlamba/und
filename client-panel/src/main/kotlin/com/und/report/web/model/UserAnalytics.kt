package com.und.report.web.model

import com.und.web.model.AggregationType
import com.und.web.model.GlobalFilterType
import java.io.Serializable

data class UserCountForProperty(var usercount: Int, var groupedBy: Map<String, Any>)

data class UserCountForTime(var usercount: Int, var time: Int)

data class UserCountTrendForDate(var date: String, var trenddata: List<UserCountForTime>)

data class UserCountByTypeForTime(var newusercount: Int, var oldusercount: Int, var time: Int)

data class UserTypeTrendForDate(var date: String, var userCountData: List<UserCountByTypeForTime>)

data class UserCountByEvent(var usercount: Int, var eventname: String)

data class UserCountByEventForDate(var date: String, var userCountData: List<UserCountByEvent>)


class GroupBy : Serializable {
    var groupFilterType: GlobalFilterType = GlobalFilterType.EventAttributeProperties
    var groupName: String = ""
}
class AggregateBy : Serializable {

    var globalFilterType: GlobalFilterType = GlobalFilterType.EventAttributeProperties
    var name: String = ""
    var aggregationType: AggregationType = AggregationType.Sum
}


