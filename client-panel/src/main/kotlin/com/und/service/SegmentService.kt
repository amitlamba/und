package com.und.service

//import com.und.model.jpa.Segment
import com.und.model.mongo.eventapi.EventUser
import com.und.web.model.EventUser as EventUserWeb
import org.springframework.stereotype.Service
import java.util.*
import com.und.web.model.Segment as WebSegment

@Service
interface SegmentService {

    fun createSegment(websegment: WebSegment): WebSegment

    fun allSegment(): List<WebSegment>

    fun segmentById(id:Long): WebSegment

    fun segmentUsers(segmentId: Long, clientId: Long): List<EventUser>

    fun segmentUsers(segment: WebSegment, clientId: Long): List<EventUserWeb>
}

