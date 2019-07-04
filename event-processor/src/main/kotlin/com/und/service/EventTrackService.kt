package com.und.service


import com.und.model.Email
import com.und.model.EmailStatus
import com.und.model.EmailStatusUpdate
import com.und.model.web.Event
import com.und.repository.jpa.ClientSettingsRepository
import com.und.repository.mongo.EmailSentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class EventTrackService {

//    @Autowired
//    private lateinit var tenantProvider: TenantProvider

//    @Autowired
////    private lateinit var eventStream: EventStream

    @Autowired
    private lateinit var emailSentRepository: EmailSentRepository

    @Autowired
    private lateinit var clientSettings:ClientSettingsRepository

    @Autowired
    private lateinit var eventSaveService: EventSaveService

  //  fun toKafka(event: Event): Boolean = eventStream.outTrackEvent().send(MessageBuilder.withPayload(event).build())

    @StreamListener("inTrackEvent")
    fun processTrackEvent(event: Event) {

        val clientId = event.clientId
        val client= clientSettings.findByClientID(clientId)
        client?.let {
            event.timeZone=it.timezone
        }
        val notificationId = event.notificationId
        val email: Email? =  emailSentRepository.findById(notificationId,clientId).orElse(null)
        //val mongoEventId = eventSaveService.saveEvent(event)
        email?.let {

            with(event) {
                event.identity.userId = email.userID
                attributes["campaign_id"] = it.campaignId?:-1L
                attributes["status"] =  EmailStatus.CTA_PERFORMED
            }
            val mongoEventId = eventSaveService.saveEvent(event)

            it.status = EmailStatus.CTA_PERFORMED
            it.statusUpdates.add(EmailStatusUpdate(LocalDateTime.now(ZoneId.of("UTC")), EmailStatus.CTA_PERFORMED, null))
            emailSentRepository.saveEmail(it)
        }


    }
}