package com.und.repository.mongo

import com.und.model.mongo.SegmentReachability
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SegmentReachabilityRepository:MongoRepository<SegmentReachability,String>,CustomSegmentReachabilityRepository {

    fun findByClientIdAndId(clientId:Long,id:Long):Optional<SegmentReachability>
}