package com.und.report.repository.mongo

import com.und.report.web.model.ReachabilityResult
import org.bson.types.ObjectId
import org.springframework.stereotype.Repository

@Repository
interface ReachabilityRepository{

    fun getReachabilityOfSegment(clientID:Long,segmentId:Long,segmentUsers:List<ObjectId>):ReachabilityResult
}