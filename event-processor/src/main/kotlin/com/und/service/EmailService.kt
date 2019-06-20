package com.und.service


import com.und.model.EmailRead
import com.und.model.EmailStatus
import com.und.model.EmailStatusUpdate
import com.und.model.web.Event
import com.und.repository.mongo.EmailSentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class EmailService {

    @Autowired
    private lateinit var emailSentRepository: EmailSentRepository

    @Value("\${und.url.event}")
    private lateinit var eventApiUrl: String

    @StreamListener("inEmailRead")
    fun processEmailRead(emailRead: EmailRead) {
        //TODO did we need this line or not
        //tenantProvider.setTenat(emailRead.clientID.toString())

        val mongoEmail: com.und.model.Email? = emailSentRepository.findById(emailRead.mongoEmailId,emailRead.clientID).get()
        if (mongoEmail != null) {
            if (mongoEmail.status.order < EmailStatus.READ.order) {
                mongoEmail.status = EmailStatus.READ
            }
            mongoEmail.statusUpdates.add(EmailStatusUpdate(LocalDateTime.now(ZoneId.of("UTC")), EmailStatus.READ, null))
            emailSentRepository.saveEmail(mongoEmail)
            //TODo ad userId
            val event = Event()
            with(event) {
                name = "Notification Read"
                clientId = emailRead.clientID
                notificationId = mongoEmail.id ?: "-1"
                attributes["campaign_id"] = mongoEmail.campaignId ?: -1L
                attributes["status"] =  mongoEmail.status
            }
            //eventService.toKafka(event)
            //TODO call all three service which process event save,build,segment process

        }
    }

}