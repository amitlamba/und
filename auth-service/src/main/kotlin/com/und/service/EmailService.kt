package com.und.service

import com.und.common.utils.loggerFor
import com.und.config.EventStream
import com.und.model.jpa.ClientVerification
import com.und.model.utils.Email
import com.und.model.jpa.security.Client
import com.und.model.jpa.security.EmailMessage
import com.und.model.jpa.security.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service

@Service
class EmailService {

    companion object {

        protected val logger = loggerFor(EmailService::class.java)
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

    private fun toKafka(email: Email) {
        eventStream.clientEmailSend().send(MessageBuilder.withPayload(email).build())
    }

}