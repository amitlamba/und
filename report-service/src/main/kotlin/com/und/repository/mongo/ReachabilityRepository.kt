package com.und.repository.mongo

import com.und.web.model.ReachabilityResult
import org.bson.types.ObjectId
import org.springframework.stereotype.Repository

@Repository
interface ReachabilityRepository{

    fun getReachabilityOfSegment(clientID:Long,segmentId:Long,segmentUsers:List<ObjectId>):ReachabilityResult
}