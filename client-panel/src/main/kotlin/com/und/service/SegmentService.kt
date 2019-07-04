package com.und.service

//import com.und.model.jpa.Segment
import com.und.model.IncludeUsers
import com.und.model.jpa.Segment
import com.und.model.mongo.eventapi.EventUser
import com.und.web.model.IdName
import org.springframework.stereotype.Service
import com.und.web.model.EventUser as EventUserWeb
import com.und.web.model.EventUserMinimal
import com.und.web.model.Segment as WebSegment

@Service
interface SegmentService {

    fun createSegment(websegment: WebSegment, clientId: Long, userId: Long): WebSegment

    fun allSegment(clientId: Long): List<WebSegment>

    fun allSegmentIdName(clientId: Long): List<IdName>

    fun segmentById(id:Long,clientId: Long?): WebSegment

    fun persistedSegmentById(id:Long,clientId: Long?): Segment

    fun segmentUserIds(segmentId: Long, clientId: Long,includeUsers: IncludeUsers): List<String>

    fun segmentUserIds(segment:WebSegment,clientId: Long,includeUsers: IncludeUsers):List<String>

    fun segmentUsers(segmentId: Long, clientId: Long,includeUsers: IncludeUsers,campaign:String?): List<EventUser>

    fun segmentUsers(segment: WebSegment, clientId: Long, userId: Long, includeUsers: IncludeUsers): List<EventUserMinimal>

    fun isUserPresentInSegment(segment: Segment, clientId: Long, userId: String,includeUsers: IncludeUsers): Boolean

    fun segmentByClientId(clientId: Long):List<Segment>
}

