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
        val events = eventRepository.findByIdentity(Identity(userId = userId))
        return events
    }

    private fun getDummyEventUser(): EventUser {
        val eventUser = EventUser()
        eventUser.clientId ="1"
        eventUser.clientUserId="1"
        eventUser.standardInfo = StandardInfo(firstName = "Amit", lastName = "Lamba", country = "India", countryCode = "IN",
                dob = "2017-01-04", gender = "Male")
        eventUser.socialId = SocialId(email = "amit@userndot.com", mobile = "8882774104")
        eventUser.id=null

        return eventUser
    }
}