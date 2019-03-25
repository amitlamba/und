package com.und.livesegment.service

import com.und.livesegment.model.jpa.LiveSegment
import com.und.livesegment.model.mongo.LiveSegmentReportCount
import com.und.livesegment.model.webmodel.WebLiveSegment
import com.und.web.model.Segment
import org.springframework.stereotype.Service

@Service
interface LiveSegmentService {

    fun findByClientIDAndStartEvent(clientId: Long, startEvent: String): List<LiveSegment>

    fun findByClientIDAndEndEvent(clientId: Long, endEvent: String): List<LiveSegment>

    fun saveLiveSegment(segment:WebLiveSegment,clientId: Long,appUserId:Long?)

    fun getLiveSegments(clientId: Long):List<WebLiveSegment>

    fun getLiveSegmentByClientIDAndId(clientId: Long,id:Long):WebLiveSegment

    fun getLiveSegmentUsersCount(clientId: Long,segmentId:Long):Long

    fun segmentValidator(segment:Segment?)

    fun getJpaLiveSegmentByClientIdAndId(clientId: Long,id: Long):LiveSegment

    fun getLiveSegmentReportByDateRange(startDate:String,endDate:String,segmentId: Long,clientId: Long):LiveSegmentReportCount
}