package com.und.repository.mongo

import com.und.config.EventStream
import com.und.eventapi.utils.logger
import com.und.model.mongo.eventapi.Event
import com.und.service.eventapi.EventService
import com.und.web.model.eventapi.Identity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.messaging.support.MessageBuilder
import java.lang.RuntimeException

class EventRepositoryUpdateImpl : EventRepositoryUpdate {

    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    @Autowired
    lateinit var eventStream:EventStream

    @Autowired
    lateinit var eventService:EventService

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
            val update = Update.update("userId", userId).set("userIdentified",true)
            mongoTemplate.updateMulti(query, update, Event::class.java)
            /***find all those event that are identified with this identity.
             * And put them in queue for live segment processing
             * **/
            val query = Query().addCriteria(Criteria
                    .where("deviceId").`is`(identity.deviceId)
                    .and("sessionId").`is`(identity.sessionId)
                    .and("userId").`is`(userId))

            processEventForLiveSegment(query)
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
            /***find all those event that are identified with this identity.
             * And put them in queue for live segment processing
             * **/
            //FIXME we are processing same event two times.
//            val query = Query().addCriteria(Criteria
//                    .where("deviceId").`is`(identity.deviceId)
//                    .and("userId").`is`(userId))
//            processEventForLiveSegment(query)
        }


    }

    private fun processEventForLiveSegment(query: Query) {
        try{
        val events = mongoTemplate.find<Event>(query)
        events.forEach {
            eventStream.outEventForLiveSegment().send(MessageBuilder.withPayload(eventService.buildEventForLiveSegment(it)).build())
        }
        }catch (ex:RuntimeException){
            logger.error("Failed To process event for live segment.")
        }
    }

    override fun save(event: Event) {
        mongoTemplate.save(event,"${event.clientId}_event")

    }
}