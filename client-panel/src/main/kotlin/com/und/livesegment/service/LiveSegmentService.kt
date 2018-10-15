package com.und.livesegment.service

import com.und.livesegment.model.jpa.LiveSegment

interface LiveSegmentService {

    fun findByClientIDAndStartEvent(clientId: Long, startEvent: String): List<LiveSegment>

    fun findByClientIDAndEndEvent(clientId: Long, endEvent: String): List<LiveSegment>
}