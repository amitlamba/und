package com.und.repository.mongo

import com.und.model.mongo.Email
import java.util.*

interface EmailSentCustomRepository {
    fun saveEmail(email: Email): Email?
    fun findById(mongoId:String?,clientId:Long): Optional<Email>
}