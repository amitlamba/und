package com.und.service

//import com.und.model.jpa.Segment
import com.und.model.jpa.Segment
import com.und.model.mongo.eventapi.EventUser
import org.springframework.stereotype.Service
import com.und.web.model.EventUser as EventUserWeb
import com.und.web.model.Segment as WebSegment

@Service
interface SegmentService {

    fun createSegment(websegment: WebSegment): WebSegment

    fun allSegment(): List<WebSegment>

    fun segmentById(id:Long,clientId: Long?): WebSegment

    fun persistedSegmentById(id:Long,clientId: Long?): Segment

    fun segmentUserIds(segmentId: Long, clientId: Long): List<String>

    fun segmentUsers(segmentId: Long, clientId: Long): List<EventUser>

    fun segmentUsers(segment: WebSegment, clientId: Long): List<EventUserWeb>

    fun isUserPresentInSegment(segment: Segment, clientId: Long, userId: String): Boolean

    fun segmentByClientId(clientId: Long):List<Segment>
}

