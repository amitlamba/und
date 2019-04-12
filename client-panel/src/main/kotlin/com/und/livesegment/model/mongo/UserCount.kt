package com.und.livesegment.model.mongo

data class UserCount ( var _id:Boolean?, var count:Long=0 )

data class CountPerDay(val totalUsersPerDay:Int,val uniqueUsersPerDay:Int,val date:String)

data class LiveSegmentReportCount(val countPerDay:List<CountPerDay>,val totalUsers:Int,val totalUniqueUsers:Int)