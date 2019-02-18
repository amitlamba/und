package com.und.repository.mongo

import org.springframework.stereotype.Repository


interface CustomSegmentReachabilityRepository {
    fun updateSegmentReachability(segmentId:Long,key:String,objectId:Int,clientId: Long)
    fun getReachabilityOfSegmentByDate(segmentId: Long,key: String,date:String,clientId:Long):Int
}