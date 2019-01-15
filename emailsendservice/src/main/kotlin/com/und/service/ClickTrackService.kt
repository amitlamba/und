package com.und.service

import com.und.model.mongo.ClickTrackEvent
import com.und.model.mongo.EmailStatus
import com.und.model.mongo.EmailStatusUpdate
import com.und.repository.mongo.ClickTrackEventRepository
import com.und.repository.mongo.EmailSentRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class ClickTrackService {
    private lateinit var clickTrackEventRepository: ClickTrackEventRepository
    private lateinit var emailSentRepository: EmailSentRepository

    fun markClickTrack(clickTrackEvent: ClickTrackEvent): ClickTrackEvent {
        val saved = clickTrackEventRepository.save(clickTrackEvent)
        val clickTrackEventId = saved.id
        if (clickTrackEvent.emailUid.isNotBlank()) {
            var email = emailSentRepository.findById(clickTrackEvent.emailUid).get()
            when (email.status) {
                EmailStatus.SENT, EmailStatus.READ -> {
                    email.statusUpdates.add(EmailStatusUpdate(LocalDateTime.now(ZoneId.of("UTC")), EmailStatus.CTA_PERFORMED, clickTrackEventId))
                    email.status = EmailStatus.CTA_PERFORMED
                    emailSentRepository.save(email)
                }
                EmailStatus.CTA_PERFORMED -> {}
                EmailStatus.NOT_SENT -> {}
            }
        }
        return clickTrackEvent
    }
}