package com.und.service.eventapi

import com.und.common.utils.MetadataUtil
import com.und.config.EventStream
import com.und.eventapi.utils.copyNonNull
import com.und.eventapi.utils.copyNonNullMongo
import com.und.model.UpdateIdentity
import com.und.model.mongo.eventapi.*
import com.und.repository.mongo.CommonMetadataRepository
import com.und.repository.mongo.EventUserRepository
import com.und.security.utils.AuthenticationUtils
import com.und.security.utils.TenantProvider
import com.und.web.model.eventapi.EventUser
import com.und.web.model.eventapi.Identity
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import java.util.*
import com.und.model.mongo.eventapi.EventUser as MongoEventUser

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

    fun checkUserExistOrNot(uId: String,clientId: Long): String? {
        var eventUser = eventUserRepository.findByIdentityUid(uId,clientId)
        if (eventUser.isPresent) {
            return eventUser.get().id
        }
        return null
    }

    fun save(eventUser: MongoEventUser): MongoEventUser {
        val clientId = eventUser.clientId
        tenantProvider.setTenat(clientId.toString())
        //FIXME save to user profile metadata
        val userProfileMetadta = buildMetadata(eventUser)
        userProfileMetadta.clientId = clientId.toLong()
        commonMetadataRepository.save(userProfileMetadta,clientId.toLong())
        return eventUserRepository.save(eventUser)
    }


    private fun buildMetadata(eventUser: MongoEventUser): CommonMetadata {
        val propertyName = "UserProperties"
        val metadata = commonMetadataRepository.findByName(propertyName,eventUser.clientId.toLong()) ?: CommonMetadata()
        metadata.name = propertyName
        val properties = MetadataUtil.buildMetadata(eventUser.additionalInfo, metadata.properties)
        metadata.properties.clear()
        metadata.properties.addAll(properties)
        return metadata
    }

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

    fun getEventUserByEventUserIdOrUid(id: String, uid: String,clientId: Long): MongoEventUser? {
        var mongoEventUser: MongoEventUser? = null
        eventUserRepository.findByIdOrIdentityUid(id, uid,clientId).ifPresent { eu -> mongoEventUser = eu }
        return mongoEventUser
    }

    @StreamListener("inEventUser")
    @SendTo("outProcessEventUserProfile")
    fun processIdentity(eventUser: EventUser): UpdateIdentity {
        tenantProvider.setTenat(eventUser.identity.clientId.toString())
        eventUser.clientId = eventUser.identity.clientId ?: -1
        var updateIdentity: UpdateIdentity = UpdateIdentity()
        val identity = eventUser.identity
        fun copyChangedValues(userId: String): MongoEventUser {
            val uid = eventUser.uid
            var existingEventUser: com.und.model.mongo.eventapi.EventUser
            if (uid != null && uid.isNotEmpty()) {
                var user = eventUserRepository.findByIdentityUid(uid,eventUser.clientId.toLong())
                if (!user.isPresent) {
                    user = eventUserRepository.findById(userId,eventUser.clientId.toLong())
                    existingEventUser = user.get()
                    updateIdentity = UpdateIdentity(find = userId, update = userId, clientId = eventUser.clientId)
                } else {
                    existingEventUser = user.get()
                    val anonymous = eventUserRepository.findById(userId,existingEventUser.clientId.toLong())
                    existingEventUser = existingEventUser.copyNonNullMongo(anonymous.get())
                    eventUserRepository.deleteById(userId,existingEventUser.clientId.toLong())
                    updateIdentity = UpdateIdentity(find = userId, update = existingEventUser.id!!, clientId = eventUser.clientId)
                }
                eventUser.identity.idf = 1

            } else {
                existingEventUser = eventUserRepository.findById(userId,eventUser.clientId.toLong()).get()
            }
//            val existingUser = /*if (existingEventUser.isPresent)*/ existingEventUser.get()
//            else {
//                val user = MongoEventUser()
//                user.creationTime = Date.from(Instant.ofEpochMilli(eventUser.creationDate).atZone(ZoneId.of("UTC")).toInstant())
//                user
//            }
//            eventUser.creationTime?.let {
////                existingUser.creationTime=Date.from(Instant.ofEpochSecond(it).atZone(ZoneId.of("UTC")).toInstant())
//                existingUser.creationTime=Date.from(Instant.ofEpochMilli(it))
//            }
//            existingUser.creationTime=Date.from(Instant.ofEpochMilli(eventUser.creationDate).atZone(ZoneId.of("UTC")).toInstant())
            return existingEventUser.copyNonNull(eventUser)
        }

        val userId = identity.userId
        if(userId != null) {
        val eventUserCopied = copyChangedValues(userId)
        val persistedUser = save(eventUserCopied)
        return updateIdentity
//            return Identity(userId = persistedUser.id, deviceId = identity.deviceId, sessionId = identity.sessionId, clientId = identity.clientId,idf = identity.idf)
        }else{
            throw IllegalArgumentException("user id should have been preset found null")
        }
    }


//    @StreamListener("inProcessEventUserProfile")
//    fun processedEventUserProfile(identity: Identity) {
//        tenantProvider.setTenat(identity.clientId.toString())
//        //println(identity)
//        eventService.updateEventWithUser(identity)
//        //update all events where session id, machine id matches and userid is absent
//        //eventUserRepository.
//        //save(identity.eventUser )
//    }

    @StreamListener("inProcessEventUserProfile")
    fun processedEventUserProfile(identity: UpdateIdentity) {
        tenantProvider.setTenat(identity.clientId.toString())
        //println(identity)
        eventService.updateEventWithUserIdentity(identity)
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


    fun logout(identity: Identity?): Identity {
        val identityCopy = identity?.copy() ?: Identity()

        with(identityCopy) {
            deviceId = if (deviceId.isEmpty()) UUID.randomUUID().toString() else deviceId
            sessionId = if (sessionId.isEmpty()) UUID.randomUUID().toString() else sessionId
        }
        //verify old data exists if device id, session id is not null
        return identityCopy
    }

    fun login(identity: Identity?): Identity {
        val identityCopy = identity?.copy() ?: Identity()

        with(identityCopy) {
            deviceId = if (deviceId.isEmpty()) UUID.randomUUID().toString() else deviceId
            sessionId = if (sessionId.isEmpty()) UUID.randomUUID().toString() else sessionId
        }
        //verify old data exists if device id, session id is not null
        return identityCopy
    }
}