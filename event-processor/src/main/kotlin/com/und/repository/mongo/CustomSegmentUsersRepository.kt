package com.und.repository.mongo

interface CustomSegmentUsersRepository {

    fun addUserInSegment(clientId:Long,userId:String,segmentId:Long)
    fun removeUserFromSegment(clientId: Long,userId: String,segmentId: Long)
}