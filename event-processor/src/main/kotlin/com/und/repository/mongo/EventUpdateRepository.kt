package com.und.repository.mongo

import com.und.model.UpdateIdentity
import com.und.model.mongo.Event
import com.und.model.web.Identity
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface EventUpdateRepository:MongoRepository<Event,String>,EventUpdateCustomRepository{
}