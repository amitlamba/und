package com.und.livesegment.service

import com.und.livesegment.model.jpa.LiveSegment
import com.und.livesegment.repository.jpa.LiveSegmentRepository
import com.und.repository.jpa.SegmentRepository
import com.und.security.utils.AuthenticationUtils
import com.und.web.controller.exception.SegmentNotFoundException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class LiveSegmentServiceImpl : LiveSegmentService {

    @Autowired
    lateinit var liveSegmentRepository: LiveSegmentRepository

    override fun findByClientIDAndStartEvent(clientId: Long, startEvent: String): List<LiveSegment> {
        val segments = liveSegmentRepository.findByClientIDAndStartEvent(clientId, startEvent)
        if (segments != null) {
            return segments
        } else {
            return emptyList()
        }
    }

    override fun findByClientIDAndEndEvent(clientId: Long, endEvent: String): List<LiveSegment> {
        val segments = liveSegmentRepository.findByClientIDAndEndEvent(clientId, endEvent)
        if (segments != null) {
            return segments
        } else {
            return emptyList()
        }
    }


}