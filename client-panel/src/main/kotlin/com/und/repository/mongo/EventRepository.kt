package com.und.repository.mongo

import com.und.model.mongo.eventapi.Event
import com.und.model.mongo.eventapi.Identity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface EventRepository :MongoRepository<Event,String> ,EventCustomRepository{

}



