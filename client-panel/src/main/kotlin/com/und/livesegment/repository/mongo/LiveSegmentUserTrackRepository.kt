package com.und.livesegment.repository.mongo

import com.und.livesegment.model.LiveSegmentUser
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface LiveSegmentUserTrackRepository:CustomLiveSegmentUserTrackRepository,MongoRepository<LiveSegmentUser,String> {
}