package com.und.service.eventapi

import com.und.config.EventStream
import com.und.repository.mongo.EventUserRepository
import com.und.security.utils.AuthenticationUtils
import com.und.web.model.eventapi.EventUser
import com.und.web.model.eventapi.Identity
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import java.util.*
import com.und.model.mongo.eventapi.EventUser as MongoEventUser

@Service
class EventUserService {


    @Autowired
    private lateinit var eventUserRepository: EventUserRepository


    @Autowired
    private lateinit var eventStream: EventStream


    fun getEventUserByEventUserId(id: String,clientId:Long): MongoEventUser? {
        var mongoEventUser: MongoEventUser? = null
        eventUserRepository.findById(id,clientId).ifPresent { eu -> mongoEventUser = eu }
        return mongoEventUser
    }

    fun getEventUserByUid(uid: String,clientId: Long): MongoEventUser? {
        var mongoEventUser: MongoEventUser? = null
        eventUserRepository.findByIdentityUid(uid,clientId).ifPresent { eu -> mongoEventUser = eu }
        return mongoEventUser
    }

    fun toKafka(eventUser: EventUser): Boolean =
            eventStream.outEventUser().send(MessageBuilder.withPayload(eventUser).build())

    /**
     * assign device id if absent
     * assign anonymous session id if absent
     * returns a copy of identity, doesn't change passed argument
     */
    fun initialiseIdentity(identity: Identity?): Identity {
        val identityCopy = identity?.copy() ?: Identity()
        val clientId= AuthenticationUtils.principal.clientId?.toInt() ?: -1
        val timeZone = AuthenticationUtils.principal.timeZoneId
        with(identityCopy) {
            deviceId = if (deviceId.isEmpty()) UUID.randomUUID().toString() else deviceId
            sessionId = if (sessionId.isEmpty()) UUID.randomUUID().toString() else sessionId
            this.clientId = clientId
            idf = 0
        }

        //creating anonymous user
        if (identityCopy.userId == null) {

            val userId = ObjectId().toString()
            identityCopy.userId = userId

            val eventUser = com.und.model.mongo.eventapi.EventUser()
            val identity_ = com.und.model.mongo.eventapi.Identity()

            with(identity_) {
                undId = userId
            }
            with(eventUser) {
                this.id = userId
                this.identity = identity_
                this.clientId = clientId
//                this.creationTime = Date.from(Instant.ofEpochMilli(System.currentTimeMillis()).atZone(ZoneId.of(timeZone)).toInstant())
            }
            eventUserRepository.save(eventUser)
        }

        return identityCopy
    }

}