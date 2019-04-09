package com.und.report.service

import com.und.model.IncludeUsers
import com.und.report.model.SegmentTrendCount
import com.und.report.web.model.Reachability
import org.springframework.stereotype.Service

@Service
interface ReachabilityService {
    fun getReachabilityBySegmentId(segmentId:Long):Reachability
    fun getReachabilityOfSegmentByDate(segmentId: Long,date:String):Int?
    fun getReachabilityOfSegmentByDateRange(clientId: Long,segmentId: Long,date1:String,date2:String):List<SegmentTrendCount>
    fun setReachabilityOfSegmentToday(segmentId: Long,clientId:Long,includeUsers: IncludeUsers):Int
    fun setReachabilityOfSegmentTodayAll(segmentId: Long,clientId:Long)
    fun checkTypeOfSegment(clientId: Long,segmentId: Long):Boolean
}