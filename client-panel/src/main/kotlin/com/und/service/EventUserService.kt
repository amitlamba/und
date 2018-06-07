package com.und.service

import com.und.repository.mongo.EventUserRepository
import com.und.security.utils.AuthenticationUtils
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
            EventUser()
        } else null
    }

    private fun getClientId(): Long {
        val clientId = AuthenticationUtils.clientID
        return clientId?:
            throw org.springframework.security.access.AccessDeniedException("User is not logged in")

    }

    fun findEventUserByEmail(id: String): EventUser? {
        val clientId = getClientId()
        val user = eventUserRepository.findUserByEmail(id,clientId)
        return if (user.isPresent) {
            EventUser()
        } else null
    }

    fun findEventUserBySysId(id: String): EventUser? {
        val clientId = getClientId()
        val user = eventUserRepository.findUserBySysId(id,clientId)
        return if (user.isPresent) {
            EventUser()
        } else null
    }

    fun findEventUserByFB(id: String): EventUser? {
        val clientId = getClientId()
        val user = eventUserRepository.findUserByFbId(id,clientId)
        return if (user.isPresent) {
            EventUser()
        } else null
    }

    fun findEventUserByMobile(id: String): EventUser? {
        val clientId = getClientId()
        val user = eventUserRepository.findUserByMobile(id,clientId)
        return if (user.isPresent) {
            EventUser()
        } else null
    }

    fun findEventUserByGoogleId(id: String): EventUser? {
        val clientId = getClientId()
        val user = eventUserRepository.findUserByGoogleId(id,clientId)
        return if (user.isPresent) {
            EventUser()
        } else null
    }


}