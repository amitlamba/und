package com.und.service.eventapi

import com.und.config.EventStream
import com.und.model.mongo.Email
import com.und.model.mongo.EmailStatus
import com.und.model.mongo.EmailStatusUpdate
import com.und.repository.jpa.ClientSettingRepository
import com.und.repository.mongo.EmailSentRepository
import com.und.security.utils.TenantProvider
import com.und.web.model.eventapi.Event
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class EventTrackService {

    @Autowired
    private lateinit var tenantProvider: TenantProvider

    @Autowired
    private lateinit var eventStream: EventStream

    @Autowired
    private lateinit var emailSentRepository: EmailSentRepository

    @Autowired
    private lateinit var clientSettings:ClientSettingRepository

    @Autowired
    private lateinit var eventService: EventService

    fun toKafka(event: Event): Boolean = eventStream.outTrackEvent().send(MessageBuilder.withPayload(event).build())

    @StreamListener("inTrackEvent")
    fun processTrackEvent(event: Event) {

        val clientId = event.clientId
        tenantProvider.setTenat(clientId.toString())
        val client= clientSettings.findByClientID(clientId)
        client.ifPresent {
            event.timeZone=it.timezone
        }
        val notificationId = event.notificationId
        val email: Email? =  emailSentRepository.findById(notificationId,clientId).orElse(null)
        //val mongoEventId = eventService.saveEvent(event)
        email?.let {

            with(event) {
                event.identity.userId = email.userID
                attributes["campaign_id"] = it.campaignId?:-1L
                attributes["status"] =  EmailStatus.CTA_PERFORMED
            }
            val mongoEventId = eventService.saveEvent(event)

            it.status = EmailStatus.CTA_PERFORMED
            it.statusUpdates.add(EmailStatusUpdate(LocalDateTime.now(ZoneId.of("UTC")), EmailStatus.CTA_PERFORMED, null))
            emailSentRepository.saveEmail(it)
        }


    }
}