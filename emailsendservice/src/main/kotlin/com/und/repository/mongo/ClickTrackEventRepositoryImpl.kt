package com.und.repository.mongo

import com.und.model.mongo.ClickTrackEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate

class ClickTrackEventRepositoryImpl:ClickTrackEventCustomRepository {
    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    override fun saveEvent(save: ClickTrackEvent): ClickTrackEvent {
        return mongoTemplate.save(save,"${save.clientId}_click_event")
    }
}