package com.und.repository.mongo

import com.und.model.mongo.Email
import com.und.model.mongo.EmailStatus
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface EmailSentCustomRepository {

    fun findById(id:String,clientId:Long): Optional<Email>

    fun save(email: Email)

    fun saveEmail(email: Email, clientId: Long)

    fun updateStatus(emailId: String, emailStatus: EmailStatus, clientId: Long, clickTrackEventId: String?)

}