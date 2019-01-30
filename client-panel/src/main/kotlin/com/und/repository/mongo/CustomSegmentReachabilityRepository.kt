package com.und.repository.mongo

import org.springframework.stereotype.Repository


interface CustomSegmentReachabilityRepository {
    fun updateSegmentReachability(segmentId:Long,key:String,objectId:Int)
    fun getReachabilityOfSegmentByDate(segmentId: Long,key: String,date:String):Int
}