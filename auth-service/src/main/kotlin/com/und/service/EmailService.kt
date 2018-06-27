package com.und.service

import com.und.common.utils.loggerFor
import com.und.config.EventStream
import com.und.model.jpa.ClientVerification
import com.und.model.jpa.ContactUs
import com.und.model.utils.Email
import com.und.model.jpa.security.Client
import com.und.model.jpa.security.EmailMessage
import com.und.model.jpa.security.User
import com.und.model.redis.security.UserCache
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import javax.mail.internet.InternetAddress

@Service
class EmailService {

    companion object {

        protected val logger = loggerFor(EmailService::class.java)
        const val forgotPasswordTemplate = 1L
        const val contactusTemplate = 2L
        const val verificationTemplate = 3L
    }

    @Autowired
    private lateinit var eventStream: EventStream

    fun sendEmail(email: Email){
        logger.info("email being sent -------------")

        logger.info("from ${email.fromEmailAddress}")
        logger.info("to ${email.toEmailAddresses}")
        logger.info("subject ${email.emailSubject}")
        logger.info("body ${email.emailBody}")
        toKafka(email)
        logger.info("email sent -------------")
    }

    fun sendForgotPasswordEmail(code: UserCache, email: String) {
        val dataMap = mutableMapOf<String, Any>(
                "code" to code,
                "resetpasswordUrl" to "resetpasswordUrl"
        )

        val email = Email(
                clientID = 1,
                toEmailAddresses = arrayOf(InternetAddress(email)),
                emailTemplateId = EmailService.forgotPasswordTemplate,
                emailTemplateName = "forgotpassword",
                data = dataMap

        )
        sendEmail(email)
    }

    fun sendContactUsEmail(contactInfo: ContactUs) {
        val dataMap = mutableMapOf<String, Any>(
        )

        val email = Email(
                clientID = 1,
                toEmailAddresses = arrayOf(InternetAddress(contactInfo.email)),
                emailTemplateId = EmailService.contactusTemplate,
                emailTemplateName = "contactus",
                data = dataMap

        )
        sendEmail(email)
    }

    fun sendVerificationEmail(client: Client) {
        val dataMap = mutableMapOf<String, Any>(
                "client" to client,
                "verificationUrl" to ""
        )

        val email = Email(
                clientID = 1,
                toEmailAddresses = arrayOf(InternetAddress(client.email)),
                emailTemplateId = EmailService.verificationTemplate,
                emailTemplateName = "verificationemail",
                data = dataMap

        )
        sendEmail(email)
    }

    private fun toKafka(email: Email) {
        eventStream.clientEmailSend().send(MessageBuilder.withPayload(email).build())
    }

}