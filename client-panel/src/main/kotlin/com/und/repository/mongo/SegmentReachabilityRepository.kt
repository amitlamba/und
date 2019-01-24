package com.und.repository.mongo

import com.und.model.mongo.SegmentReachability
import org.springframework.data.mongodb.repository.MongoRepository
import java.time.LocalDate
import java.util.*

interface SegmentReachabilityRepository:MongoRepository<SegmentReachability,String> {

    fun findByClientIdAndSegmentIdAndDate(clientId:Long,segmentId:Long,date: LocalDate):Optional<SegmentReachability>
}