package com.und.repository.mongo

import com.und.model.mongo.Sms
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface SmsSentRepository:MongoRepository<Sms,String> {
}