package com.und.service

import com.und.common.utils.loggerFor
import com.und.config.EventStream
import com.und.exception.EmailError
import com.und.model.Email
import com.und.model.EmailRead
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import org.springframework.util.concurrent.ListenableFutureCallback
import javax.mail.internet.InternetAddress


@Service
class EmailService {

    companion object {

        protected val logger = loggerFor(EmailService::class.java)
    }

    @Autowired
    private lateinit var eventStream: EventStream

    @Autowired
    private lateinit var clientService: ClientService

    @Autowired
    private lateinit var kafkaTemplateEmailRead: KafkaTemplate<String, EmailRead>

    private val emailConnectionErrorTemplate: Long = 5L

    //@Value(value = "\${kafka.topic.email}")
    private var topic: String = "Email"

    private var emailReadTopic: String = "EmailRead"


    fun trackEmailRead(emailRead: EmailRead): EmailRead {
        val future = kafkaTemplateEmailRead.send(emailReadTopic, emailRead.clientID.toString(), emailRead)
        future.addCallback(object : ListenableFutureCallback<SendResult<String, EmailRead>> {
            override fun onSuccess(result: SendResult<String, EmailRead>) {
                logger.debug("Sent message: " + result)
            }

            override fun onFailure(ex: Throwable) {
                logger.error("Failed to send message", ex.message)
            }
        })
        return emailRead
    }


    fun sendEmail(email: Email) {
        logger.info("email being sent -------------")

        logger.info("from ${email.fromEmailAddress}")
        logger.info("to ${email.toEmailAddresses}")
        logger.info("subject ${email.emailSubject}")
        logger.info("body ${email.emailBody}")
        toKafka(email)
        logger.info("email sent -------------")
    }

    fun sendEmailConnectionErrorEmail(emailError: EmailError) {
        emailError.clientid?.let {clientId->
            val client = clientService.getClientByClientId(clientId)


            val dataMap = mutableMapOf<String, Any>(
                    "name" to "${client.firstname} ${client.lastname}",
                    "error" to "${emailError.causeMessage}"

            )

            val email = Email(
                    clientID = 1,
                    toEmailAddresses = arrayOf(InternetAddress(client.email)),
                    emailTemplateId = emailConnectionErrorTemplate,
                    emailTemplateName = "emailConnectionError",
                    data = dataMap
            )
            sendEmail(email)
        }
    }


    private fun toKafka(email: Email) {
        eventStream.clientEmailSend().send(MessageBuilder.withPayload(email).build())
    }

}