package com.und.report.web.model

data class TrendCount(var usercount: Int, var name: String)

data class TrendByTime(var usercount: Int, var time: Int)

data class TrendTimeSeries(var date: String, var trendata: List<TrendByTime>)

data class UserCountByTime(var newusercount: Int, var oldusercount: Int, var time: Int)

data class UserCountTimeSeries(var date: String, var userCountData: List<UserCountByTime>)

data class UserCountByEvent(var usercount: Int, var eventname: String)

data class UserCountByEventTimeSeries(var date: String, var userCountData: List<UserCountByEvent>)


