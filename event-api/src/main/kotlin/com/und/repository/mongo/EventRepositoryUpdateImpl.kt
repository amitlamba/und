package com.und.repository.mongo

import com.und.config.EventStream
import com.und.eventapi.utils.logger
import com.und.model.UpdateIdentity
import com.und.model.mongo.eventapi.Event
import com.und.service.eventapi.EventService
import com.und.web.model.eventapi.Identity
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.messaging.support.MessageBuilder
import java.lang.RuntimeException
import java.util.*

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
        identity.userId?.let {

            val query = Query().addCriteria(Criteria
                    .where("deviceId").`is`(identity.deviceId)
                    .and("sessionId").`is`(identity.sessionId)
                    .and("userIdentified").`is`(false)
//                    .and("userId").exists(false)
            )

            val events = mongoTemplate.find(query, Event::class.java)
            if (events.isNotEmpty()) {
                val update = Update.update("userId", it).set("userIdentified", true)
                updateAndDelete(query, update, events)
                /***find all those event that are identified with this identity.
                 * And put them in queue for live segment processing
                 * **/
//                val query1 = Query().addCriteria(Criteria
//                        .where("deviceId").`is`(identity.deviceId)
//                        .and("sessionId").`is`(identity.sessionId)
//                        .and("userId").`is`(it))
//
//                processEventForLiveSegment(query1)
            } else {

                val queryWithoutSession = Query().addCriteria(Criteria
                        .where("deviceId").`is`(identity.deviceId)
                        .and("sessionId").exists(false)
                        .and("userIdentified").`is`(false)
//                        .and("userId").exists(false)
                )

                val events = mongoTemplate.find(queryWithoutSession, Event::class.java)
                if (events.isNotEmpty()) {
                    val updateSession = Update.update("sessionId", identity.sessionId)
                    updateSession.set("userId", it)
                    updateSession.set("userIdentified",true)
                    updateAndDelete(queryWithoutSession, updateSession, events)
                    /***find all those event that are identified with this identity.
                     * And put them in queue for live segment processing
                     * **/
//                    val query = Query().addCriteria(Criteria
//                            .where("deviceId").`is`(identity.deviceId)
//                            .and("userId").`is`(it))
//                    //TODO all event have userId NO need
//                    processEventForLiveSegment(query)
                }
            }


        }


    }

    override fun updateEventsWithIdentityMatching(identity: UpdateIdentity) {
        if(identity.find.isNotEmpty()){
            val query = Query().addCriteria(Criteria.where("userId").`is`(identity.find))
            val update = Update.update("userId", identity.update).set("userIdentified", true)
            mongoTemplate.updateMulti(query,update,"${identity.clientId}_event")
        }
    }

    //FIXME must be in transaction
    private fun updateAndDelete(query: Query, update: Update, events: MutableList<Event>) {
        mongoTemplate.updateMulti(query, update, Event::class.java)
        val ids = events.map {
            ObjectId(it.userId)
        }

        val removeQuery = Query().addCriteria(Criteria("_id").`in`(ids))
        mongoTemplate.remove(removeQuery, "${events[0].clientId}_eventUser")
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

    override fun findByName(eventName: String,clientId:Long): List<Event> {
        return mongoTemplate.find(Query.query(Criteria.where("name").`is`(eventName)),"${clientId}_event")
    }

    override fun findById(id: String, clientId: Long): Optional<Event> {
        val event = mongoTemplate.findById(id,Event::class.java,"${clientId}_event")
        return if(event == null) Optional.empty() else Optional.of(event)
    }
}