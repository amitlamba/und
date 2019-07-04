package com.und.repository.mongo

import com.und.model.mongo.EventUser
import org.springframework.data.mongodb.core.query.Criteria

interface CustomSegmentUsersRepository {

    fun addUserInSegment(clientId:Long,userId:String,segmentId:Long)
    fun removeUserFromSegment(clientId: Long,userId: String,segmentId: Long)
    fun findUserByUserProperties(query:Criteria,clientId: Long):List<EventUser>
    fun isUserPresent(userId: String,clientId: Long,segmentId: Long):Boolean
}