package com.und.repository.mongo

import com.und.model.mongo.EventUser
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface EventUserRepository : MongoRepository<EventUser, String> {


}