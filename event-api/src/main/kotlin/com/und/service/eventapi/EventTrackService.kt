package com.und.service.eventapi

import com.und.config.EventStream
import com.und.model.mongo.Email
import com.und.model.mongo.EmailStatus
import com.und.model.mongo.EmailStatusUpdate
import com.und.repository.mongo.EmailSentRepository
import com.und.security.utils.TenantProvider
import com.und.web.model.eventapi.Event
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class EventTrackService {

    @Autowired
    private lateinit var tenantProvider: TenantProvider

    @Autowired
    private lateinit var eventStream: EventStream

    @Autowired
    private lateinit var emailSentRepository: EmailSentRepository

    @Autowired
    private lateinit var eventService: EventService

    fun toKafka(event: Event): Boolean = eventStream.outTrackEvent().send(MessageBuilder.withPayload(event).build())

    @StreamListener("inTrackEvent")
    fun processTrackEvent(event: Event) {

        val clientId = event.clientId
        tenantProvider.setTenat(clientId.toString())

        val mongoEmailId = event.attributes["und_mongo_email_id"].toString()
        var email: Email? = null
        emailSentRepository.findById(mongoEmailId).ifPresent({ e -> email = e })
        event.identity.userId = email?.userID

        val mongoEventId = eventService.saveEvent(event)

        email?.emailStatus = EmailStatus.CTA_PERFORMED
        email?.statusUpdates?.add(EmailStatusUpdate(LocalDateTime.now(), EmailStatus.CTA_PERFORMED, mongoEventId))
        emailSentRepository.save(email)
    }
}