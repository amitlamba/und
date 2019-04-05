package com.und.service

//import com.und.model.jpa.Segment
import com.und.model.IncludeUsers
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

    fun segmentUserIds(segmentId: Long, clientId: Long,includeUsers: IncludeUsers): List<String>

    fun segmentUsers(segmentId: Long, clientId: Long,includeUsers: IncludeUsers): List<EventUser>

    fun segmentUsers(segment: WebSegment, clientId: Long,includeUsers: IncludeUsers): List<EventUserWeb>

    fun isUserPresentInSegment(segment: Segment, clientId: Long, userId: String,includeUsers: IncludeUsers): Boolean

    fun segmentByClientId(clientId: Long):List<Segment>
}

