package com.und.campaign.repository.mongo

import com.und.model.mongo.EventUserRecord
import org.springframework.data.mongodb.repository.MongoRepository

interface EventUserRecordRepository:MongoRepository<EventUserRecord,String> {
}