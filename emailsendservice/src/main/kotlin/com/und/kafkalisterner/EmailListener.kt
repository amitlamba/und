package com.und.kafkalisterner

import com.und.exception.EmailFailureException
import com.und.factory.EmailServiceProviderConnectionFactory
import com.und.model.utils.Email
import com.und.model.utils.EmailUpdate
import com.und.service.EmailHelperService
import com.und.service.EmailService
import com.und.utils.loggerFor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.stereotype.Service

@Service
class EmailListener {


    @Autowired
    private lateinit var emailService: EmailService

    @Autowired
    private lateinit var emailHelperService: EmailHelperService

    @Autowired
    private lateinit var emailServiceProviderCredentialsFactory: EmailServiceProviderConnectionFactory

    companion object {
        val logger = loggerFor(EmailListener::class.java)
    }

    @StreamListener("emailEventSend")
    fun sendEmailCampaign(email: Email) {
        sendEmail(email)
    }

    @StreamListener("clientEmailReceive")
    fun sendClientEmail(email: Email) {
        email.clientID = 1

        sendEmail(email)
    }

    private fun sendEmail(email: Email) {
        var retry = false
        do {
            try {
                emailService.sendEmail(email)
            } catch (ef: EmailFailureException) {
                if (ef.error.retry && ef.error.retries <= 3) {
                    retry = true
                } else {
                    retry = false
                    emailService.toKafkaEmailError(ef.error)
                }
            }
        } while (retry)
    }

    @StreamListener("EmailUpdateReceive")
    fun listenEmailUpdate(emailUpdate: EmailUpdate) {
        try {
            emailHelperService.updateEmailStatus(
                    emailUpdate.mongoEmailId,
                    emailUpdate.emailStatus,
                    emailUpdate.clientID,
                    emailUpdate.eventId)
        } catch (ex: Exception) {
            logger.error("Error while Updating Email $emailUpdate", ex.message)
        }
    }

    @StreamListener(value = "VerificationEmailReceive")
    fun sendVerificationEmail(email: Email) {
        emailService.sendVerificationEmail(email)
    }
}
