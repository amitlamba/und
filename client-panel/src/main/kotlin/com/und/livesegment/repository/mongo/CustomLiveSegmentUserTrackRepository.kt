package com.und.livesegment.repository.mongo

import org.springframework.stereotype.Repository

@Repository
interface CustomLiveSegmentUserTrackRepository {

    fun findCountByClientIdAndLiveSegmentId(clientId:Long,liveSegmentId:Long):Long
    fun findCountByClientIdAndSegmentId(clientId:Long,segmentId:Long):Long
    fun getLiveSegmentReportByDateRange(startDate:String,endDate:String,clientId: Long,segmentId: Long):List<LiveSegmentResult>
}