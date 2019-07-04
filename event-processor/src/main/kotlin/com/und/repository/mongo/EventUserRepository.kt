package com.und.repository

import com.und.model.mongo.EventUser
import com.und.repository.mongo.EventUserCustomRepository
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface EventUserRepository : MongoRepository<EventUser,String>, EventUserCustomRepository {

}
