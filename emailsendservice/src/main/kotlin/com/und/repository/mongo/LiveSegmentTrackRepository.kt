package com.und.repository.mongo

import com.und.model.mongo.LiveSegmentTrack
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface LiveSegmentTrackRepository : MongoRepository<LiveSegmentTrack, String> {



}



