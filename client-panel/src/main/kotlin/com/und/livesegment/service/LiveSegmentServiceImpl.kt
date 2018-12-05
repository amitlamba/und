package com.und.livesegment.service

import com.und.livesegment.model.jpa.LiveSegment
import com.und.livesegment.repository.jpa.LiveSegmentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class LiveSegmentServiceImpl : LiveSegmentService {

    @Autowired
    lateinit var liveSegmentRepository: LiveSegmentRepository

    override fun findByClientIDAndStartEvent(clientId: Long, startEvent: String): List<LiveSegment> {
        val segments = liveSegmentRepository.findByClientIDAndStartEvent(clientId, startEvent)
        return segments?: emptyList()

    }

    override fun findByClientIDAndEndEvent(clientId: Long, endEvent: String): List<LiveSegment> {
        val segments = liveSegmentRepository.findByClientIDAndEndEvent(clientId, endEvent)
        return segments?: emptyList()
    }


}