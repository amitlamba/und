package com.und.service

import com.und.repository.mongo.EventUserRepository
import com.und.security.utils.AuthenticationUtils
import com.und.model.mongo.eventapi.EventUser as EventUserMongo
import com.und.web.model.EventUser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class EventUserService {

    @Autowired
    private lateinit var eventUserRepository: EventUserRepository

    fun findEventUserById(id: String): EventUser? {
        val clientId = getClientId()
        val user = eventUserRepository.findUserById(id, clientId)
        return if (user.isPresent) {
            buildEventUser(user.get())
        } else null
    }

    fun findEventUserByEmail(id: String): EventUser? {
        val clientId = getClientId()
        val user = eventUserRepository.findUserByEmail(id, clientId)
        return if (user.isPresent) {
            buildEventUser(user.get())
        } else null
    }

    fun findEventUserBySysId(id: String): EventUser? {
        val clientId = getClientId()
        val user = eventUserRepository.findUserBySysId(id, clientId)
        return if (user.isPresent) {
            buildEventUser(user.get())
        } else null
    }

    fun findEventUserByFB(id: String): EventUser? {
        val clientId = getClientId()
        val user = eventUserRepository.findUserByFbId(id, clientId)
        return if (user.isPresent) {
            buildEventUser(user.get())
        } else null
    }

    fun findEventUserByMobile(id: String): EventUser? {
        val clientId = getClientId()
        val user = eventUserRepository.findUserByMobile(id, clientId)
        return if (user.isPresent) {
            buildEventUser(user.get())
        } else null
    }

    fun findEventUserByGoogleId(id: String): EventUser? {
        val clientId = getClientId()
        val user = eventUserRepository.findUserByGoogleId(id, clientId)
        return if (user.isPresent) {
            buildEventUser(user.get())
        } else null
    }

    private fun getClientId(): Long {
        val clientId = AuthenticationUtils.clientID
        return clientId ?: throw org.springframework.security.access.AccessDeniedException("User is not logged in")

    }

    private fun buildEventUser(eventUserMongo: EventUserMongo): EventUser {

        val eventUser = EventUser()
        eventUser.firstName = eventUserMongo.standardInfo.firstname
        eventUser.lastName = eventUserMongo.standardInfo.lastname
        eventUser.gender = eventUserMongo.standardInfo.gender
        eventUser.country = eventUserMongo.standardInfo.country
        eventUser.city = eventUserMongo.standardInfo.city
        eventUser.address = eventUserMongo.standardInfo.address
        eventUser.clientId = eventUserMongo.clientId
        eventUser.additionalInfo = eventUserMongo.additionalInfo
        eventUser.clientUserId = eventUserMongo.identity.clientUserId
        eventUser.dob = eventUserMongo.standardInfo.dob
        eventUser.creationDate = eventUserMongo.creationTime
        eventUser.email = eventUserMongo.identity.email
        eventUser.fbId = eventUserMongo.identity.fbId
        eventUser.googleId = eventUserMongo.identity.googleId
        eventUser.mobile = eventUserMongo.identity.mobile
        eventUser.undId = eventUserMongo.identity.undId
        eventUser.countryCode = eventUserMongo.standardInfo.countryCode
        eventUser.communication=eventUserMongo.communication
        return eventUser
    }

}
