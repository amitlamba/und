package com.und.repository.mongo

import com.und.model.mongo.ClickTrackEvent
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface ClickTrackEventRepository:MongoRepository<ClickTrackEvent,String> {
}