package com.und.livesegment.service

import com.und.livesegment.model.jpa.LiveSegment
import com.und.livesegment.model.webmodel.WebLiveSegment
import org.springframework.stereotype.Service

@Service
interface LiveSegmentService {

    fun findByClientIDAndStartEvent(clientId: Long, startEvent: String): List<LiveSegment>

    fun findByClientIDAndEndEvent(clientId: Long, endEvent: String): List<LiveSegment>

    fun saveLiveSegment(segment:WebLiveSegment,clientId: Long,appUserId:Long?)

    fun getLiveSegments(clientId: Long):List<WebLiveSegment>

    fun getLiveSegmentByClientIDAndId(clientId: Long,id:Long):WebLiveSegment
}