package com.und.service.eventapi

import com.und.model.mongo.Email
import com.und.model.mongo.eventapi.Communication
import com.und.model.mongo.eventapi.CommunicationDetails
import com.und.repository.mongo.EmailSentRepository
import com.und.security.utils.TenantProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UnsubscribeService {

    @Autowired
    private lateinit var tenantProvider: TenantProvider

    @Autowired
    private lateinit var emailSentRepository: EmailSentRepository

    @Autowired
    private lateinit var eventUserService: EventUserService

    fun unsubscribeUserFromEmail(clientId: Int, mongoEmailId: String): Boolean {
        tenantProvider.setTenat(clientId.toString())
        var email: Email? = null
        emailSentRepository.findById(mongoEmailId,clientId.toLong()).ifPresent({ e -> email = e })

        if(email == null)
            return false

        val eventUser = eventUserService.getEventUserByEventUserId(email?.userID!!,clientId.toLong())
        if(eventUser != null) {
            if(eventUser.communication == null)
                eventUser.communication = Communication()
            if(eventUser.communication?.email == null)
                eventUser.communication?.email = CommunicationDetails(eventUser.identity.email!!, true)
            else
                eventUser.communication?.email?.dnd = true
            return true
        }
        return false
    }
}