package com.und.repository.mongo

import org.springframework.stereotype.Repository
import java.time.LocalDateTime


interface CustomSegmentReachabilityRepository {
    fun updateSegmentReachability(segmentId:Long,key:String,objectId:Int,clientId: Long,modifiedTime:LocalDateTime,timeZone:String)
    fun getReachabilityOfSegmentByDate(segmentId: Long,key: String,date:String,clientId:Long):Int
}