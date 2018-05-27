package com.und.service.eventapi

import com.und.common.utils.MetadataUtil
import com.und.config.EventStream
import com.und.web.model.eventapi.EventUser
import com.und.model.mongo.eventapi.EventUser as MongoEventUser
import com.und.web.model.eventapi.Identity
import com.und.repository.mongo.EventUserRepository
import com.und.security.utils.TenantProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import com.und.eventapi.utils.copyNonNull
import com.und.model.mongo.eventapi.*
import com.und.repository.mongo.CommonMetadataRepository
import com.und.repository.mongo.EventMetadataRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Service
class EventUserService {

    @Autowired
    lateinit var tenantProvider: TenantProvider

    @Autowired
    private lateinit var eventUserRepository: EventUserRepository

    @Autowired
    private lateinit var commonMetadataRepository: CommonMetadataRepository

    @Autowired
    private lateinit var eventService: EventService

    @Autowired
    private lateinit var eventStream: EventStream


    fun save(eventUser: MongoEventUser): MongoEventUser {
        val clientId = eventUser.clientId
        tenantProvider.setTenat(clientId.toString())
        //FIXME save to user profile metadata
         val userProfileMetadta = buildMetadata(eventUser)
        commonMetadataRepository.save(userProfileMetadta)
        return eventUserRepository.save(eventUser)
    }


    private fun buildMetadata(eventUser: MongoEventUser): CommonMetadata {
        val propertyName = "userProperties"
        val metadata = commonMetadataRepository.findByName(propertyName) ?: CommonMetadata()
        metadata.name = propertyName
        val properties = MetadataUtil.buildMetadata(eventUser.additionalInfo, metadata.properties)
        metadata.properties.clear()
        metadata.properties.addAll(properties)
        return metadata
    }

    fun getEventUserByEventUserId(id: String): MongoEventUser? {
        var mongoEventUser: MongoEventUser? = null
        eventUserRepository.findById(id).ifPresent({eu -> mongoEventUser = eu})
        return mongoEventUser
    }

    @StreamListener("inEventUser")
    @SendTo("outProcessEventUserProfile")
    fun processIdentity(eventUser: EventUser): Identity {
        tenantProvider.setTenat(eventUser.identity.clientId.toString())
        eventUser.clientId = eventUser.identity.clientId?:-1

        val identity = eventUser.identity
        fun copyChangedValues(userId: String): MongoEventUser {

            val existingEventUser = eventUserRepository.findById(userId)
            val existingUser = if (existingEventUser.isPresent) existingEventUser.get() else MongoEventUser()
            return existingUser.copyNonNull(eventUser)
        }

        val userId = identity.userId
        if(userId != null) {
            val eventUserCopied = copyChangedValues(userId)
            val persistedUser = save(eventUserCopied)
            return Identity(userId = persistedUser.id, deviceId = identity.deviceId, sessionId = identity.sessionId, clientId = identity.clientId)
        }else{
            throw IllegalArgumentException("user id should have been preset found null")
        }
    }


    @StreamListener("inProcessEventUserProfile")
    fun processedEventUserProfile(identity: Identity) {
        tenantProvider.setTenat(identity.clientId.toString())
        //println(identity)
        eventService.updateEventWithUser(identity)
        //update all events where session id, machine id matches and userid is absent
        //eventUserRepository.
        //save(identity.eventUser )
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

        with(identityCopy) {
            deviceId = if (deviceId.isNullOrEmpty()) UUID.randomUUID().toString() else deviceId
            sessionId = if (sessionId.isNullOrEmpty()) UUID.randomUUID().toString() else sessionId
        }
        //TODO verify old data exists if device id, session id is not null
        return identityCopy
    }


    fun logout(identity: Identity?): Identity {
        val identityCopy = identity?.copy() ?: Identity()

        with(identityCopy) {
            deviceId = if (deviceId.isNullOrEmpty()) UUID.randomUUID().toString() else deviceId
            sessionId = if (sessionId.isNullOrEmpty()) UUID.randomUUID().toString() else sessionId
        }
        //verify old data exists if device id, session id is not null
        return identityCopy
    }

    fun login(identity: Identity?): Identity {
        val identityCopy = identity?.copy() ?: Identity()

        with(identityCopy) {
            deviceId = if (deviceId.isNullOrEmpty()) UUID.randomUUID().toString() else deviceId
            sessionId = if (sessionId.isNullOrEmpty()) UUID.randomUUID().toString() else sessionId
        }
        //verify old data exists if device id, session id is not null
        return identityCopy
    }
}