package com.und.repository.mongo

import com.und.model.mongo.BlockedEmail
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface BlockedEmailRepository : MongoRepository<BlockedEmail, String>, BlockedEmailCustomRepository {
}



