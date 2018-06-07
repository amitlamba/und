package com.und.service

import com.und.repository.mongo.EventRepository
import com.und.repository.mongo.EventUserRepository
import com.und.model.mongo.eventapi.*
import com.und.security.utils.TenantProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ClientUsersService {

    @Autowired
    private lateinit var eventUserRepository: EventUserRepository
    @Autowired
    private lateinit var eventRepository: EventRepository

    fun getEventUsers(clientId: Long): List<EventUser> {
        TenantProvider().setTenat(clientId.toString())
        var eventUsers = eventUserRepository.findAll()
//        eventUserRepository.save(getDummyEventUser())
//        eventUsers = listOf(getDummyEventUser())
        return eventUsers
    }

    fun getEventUsersEvents(clientId: Long, userId: String, clientUserId: String): List<Event> {
        TenantProvider().setTenat(clientId.toString())
        val identity = Identity()
        identity.undId = userId
        val events = eventRepository.findByIdentity(identity)
        return events
    }


}