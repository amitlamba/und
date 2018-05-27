package com.und.repository.mongo

import com.und.model.mongo.eventapi.Event
import com.und.web.model.eventapi.Identity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update

class EventRepositoryUpdateImpl : EventRepositoryUpdate {

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    override fun findEventsMatchingIdentity(identity: Identity): List<Event> {
        //FIXME query is empty
        val events = mongoTemplate.find(Query(), Event::class.java)
        return events
    }

    override fun updateEventsWithIdentityMatching(identity: Identity) {
        val userId = identity.userId
        val query = Query().addCriteria(Criteria
                .where("deviceId").`is`(identity.deviceId)
                .and("sessionId").`is`(identity.sessionId)
                .and("userId").exists(false)
        )

        if(userId!= null) {
            val update = Update.update("userId", userId)
            mongoTemplate.updateMulti(query, update, Event::class.java)
        }


        val queryWithoutSession = Query().addCriteria(Criteria
                .where("deviceId").`is`(identity.deviceId)
                .and("sessionId").exists(false)
                .and("userId").exists(false)
        )

        val updateSession = Update.update("sessionId", identity.sessionId)
        if(userId != null) {
            updateSession.set("userId", userId)
            mongoTemplate.updateMulti(queryWithoutSession, updateSession, Event::class.java)
        }


    }
}