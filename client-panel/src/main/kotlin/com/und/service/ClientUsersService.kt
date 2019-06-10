package com.und.service

import com.und.model.mongo.eventapi.EventUser
import com.und.repository.mongo.EventUserRepository
import com.und.security.utils.TenantProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ClientUsersService {

    @Autowired
    private lateinit var eventUserRepository: EventUserRepository


    fun getEventUsers(clientId: Long): List<EventUser> {
        TenantProvider().setTenat(clientId.toString())
        return eventUserRepository.findAll(clientId)

    }




}