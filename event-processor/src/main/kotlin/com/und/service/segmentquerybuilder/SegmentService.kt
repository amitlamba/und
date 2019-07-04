package com.und.service.segmentquerybuilder

import com.und.model.GlobalFilter
import com.und.model.IncludeUsers
import com.und.model.Segment
import org.springframework.stereotype.Service


@Service
interface SegmentService {

    //fun createSegment(websegment: WebSegment, clientId: Long, userId: Long): WebSegment

    //fun allSegment(clientId: Long): List<WebSegment>

    //fun allSegmentIdName(clientId: Long): List<IdName>

    //fun segmentById(id:Long,clientId: Long?): WebSegment

    //fun persistedSegmentById(id:Long,clientId: Long?): Segment

    //fun segmentUserIds(segmentId: Long, clientId: Long,includeUsers: IncludeUsers): List<String>

    //fun segmentUsers(segmentId: Long, clientId: Long,includeUsers: IncludeUsers,campaign:String?): List<EventUser>

    //fun segmentUsers(segment: WebSegment, clientId: Long, userId: Long, includeUsers: IncludeUsers): List<EventUserMinimal>

    fun addUserInSegment(userId: String,clientId: Long,segmentId:Long)

    fun removeUserFromSegment(userId: String,clientId: Long,segmentId: Long)

    fun isUserPresent(userId: String,clientId: Long,segmentId: Long):Boolean

    fun isUserPropertyMatch(userId: String,clientId: Long,filter:List<GlobalFilter>,userIdentified:Boolean,timezne:String):Boolean

    //fun isUserPresentInSegment(segment: Segment, clientId: Long, userId: String,includeUsers: IncludeUsers): Boolean

    //fun segmentByClientId(clientId: Long):List<Segment>

    fun isUserPresentInSegment(segment: Segment, clientId: Long, includeUsers: IncludeUsers,campaign:String?,userId:String): Boolean

    fun isUserPresentInSegmentWithoutUserProp(segment: Segment, clientId: Long, includeUsers: IncludeUsers,campaign:String?,userId:String): Boolean
    fun isUserPresentInSegmentWithUserPropOnly(segment: Segment, clientId: Long, includeUsers: IncludeUsers,campaign:String?,userId:String): Boolean
}

