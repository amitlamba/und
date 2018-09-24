package com.und.repository.mongo

import com.und.model.mongo.Email
import com.und.model.mongo.EmailStatus
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface EmailSentCustomRepository {

    fun saveEmail(email: Email, clientId: Long)

    fun updateStatus(emailId: String, emailStatus: EmailStatus, clientId: Long, clickTrackEventId: String?)

}