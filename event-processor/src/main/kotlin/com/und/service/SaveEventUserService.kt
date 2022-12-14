package com.und.service

import com.und.model.UpdateIdentity
import com.und.model.web.EventUser
import com.und.model.mongo.EventUser as MongoEventUser
import com.und.repository.EventUserRepository
import com.und.repository.mongo.MetadataRepository
import com.und.repository.mongo.SegmentUsersRepository
import com.und.utils.Constants
import com.und.utils.copyNonNull
import com.und.utils.copyNonNullMongo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Service


@Service
class SaveEventUserService {

    @Autowired
    private lateinit var eventUserRepository: EventUserRepository

    @Autowired
    private lateinit var eventSaveService: EventSaveService

    @Autowired
    private lateinit var segmentUsersRepository: SegmentUsersRepository

    @Autowired
    private lateinit var metadataRepository: MetadataRepository

    @StreamListener(Constants.SAVE_USER)
    @SendTo(Constants.OUT_EVENTUSER)
    fun saveEventUser(eventUser: EventUser): UpdateIdentity {
        eventUser.clientId = eventUser.identity.clientId ?: -1
        var updateIdentity: UpdateIdentity = UpdateIdentity()
        val identity = eventUser.identity

        fun copyChangedValues(userId: String): MongoEventUser {
            val uid = eventUser.uid
            var existingEventUser: com.und.model.mongo.EventUser
            if (uid != null && uid.isNotEmpty()) {
                var user = eventUserRepository.findByIdentityUid(uid, eventUser.clientId.toLong())
                if (!user.isPresent) {
                    user = eventUserRepository.findById(userId, eventUser.clientId.toLong())
                    existingEventUser = user.get()
                    updateIdentity = UpdateIdentity(find = userId, update = userId, clientId = eventUser.clientId)
                } else {
                    existingEventUser = user.get()
                    val anonymous = eventUserRepository.findById(userId, existingEventUser.clientId.toLong())
                    existingEventUser = existingEventUser.copyNonNullMongo(anonymous.get())
                    eventUserRepository.deleteById(userId, existingEventUser.clientId.toLong())
                    updateIdentity = UpdateIdentity(find = userId, update = existingEventUser.id!!, clientId = eventUser.clientId)
                    updateSegmentUsers(existingEventUser, userId)
                }
                eventUser.identity.idf = 1

            } else {
                existingEventUser = eventUserRepository.findById(userId, eventUser.clientId.toLong()).get()
            }
            return existingEventUser.copyNonNull(eventUser)
        }

        val userId = identity.userId
        if (userId != null) {
            val eventUserCopied = copyChangedValues(userId)
            eventUserRepository.save(eventUserCopied)
            return updateIdentity
        } else {
            throw IllegalArgumentException("user id should have been preset found null")
        }
    }

    /**
     *  if segment is dead then add new user id and remove previous user id of anonymous user else only remove user id.
     *  decision to add left on segment computation
     */
    fun updateSegmentUsers(existingEventUser: com.und.model.mongo.EventUser, userId: String) {
        val metadatas = metadataRepository.findByClientId(existingEventUser.clientId.toLong())
        metadatas.forEach { metadata ->

            if (metadata.stopped) {
                segmentUsersRepository.removeUserFromSegment(existingEventUser.clientId.toLong(), userId, metadata.id!!)
                segmentUsersRepository.addUserInSegment(existingEventUser.clientId.toLong(), existingEventUser.id!!, metadata.id!!)
            } else {
                segmentUsersRepository.removeUserFromSegment(existingEventUser.clientId.toLong(), userId, metadata.id!!)
            }

        }
    }

    @StreamListener("inProcessEventUserProfile")
    fun processedEventUserProfile(identity: UpdateIdentity) {
        //tenantProvider.setTenat(identity.clientId.toString())
        //println(identity)
        eventSaveService.updateEventWithUserIdentity(identity)
        //update all events where session id, machine id matches and userid is absent
        //eventUserRepository.
        //save(identity.eventUser )
    }
}