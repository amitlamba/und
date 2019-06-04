package com.und.repository.mongo

import com.und.model.mongo.Email
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface EmailSentRepository :MongoRepository<Email,String>,EmailSentCustomRepository{

}