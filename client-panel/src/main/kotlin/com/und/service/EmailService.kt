package com.und.service

import com.und.common.utils.loggerFor
import com.und.config.EventStream
import com.und.exception.EmailError
import com.und.model.Email
import com.und.model.EmailRead
import com.und.repository.jpa.ClientSettingsEmailRepository
import com.und.web.controller.exception.CustomException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.annotation.Cacheable
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

    @Value(value = "\${und.system.email.setting.id}")
    private var clientSettingId:Long?=null

    @Autowired
    private lateinit var eventStream: EventStream

    @Autowired
    private lateinit var clientService: ClientService

    @Autowired
    private lateinit var kafkaTemplateEmailRead: KafkaTemplate<String, EmailRead>

    @Autowired
    private lateinit var clientSettingsEmailRepository: ClientSettingsEmailRepository

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
        toKafka(email)
        logger.info("email sent -------------")
    }

    fun sendEmailConnectionErrorEmail(emailError: EmailError) {
        val clientSetting=getClientEmailSettings((clientSettingId)?:-1)
        emailError.clientid?.let {clientId->
            val client = clientService.getClientByClientId(clientId)

            client?.let {
                val dataMap = mutableMapOf<String, Any>(
                        "name" to "${it.firstname} ${it.lastname}",
                        "error" to "${emailError.causeMessage}"

                )

                val email = Email(
                        clientID = 1,
                        fromEmailAddress = InternetAddress(clientSetting),
                        toEmailAddresses = arrayOf(InternetAddress(it.email)),
                        emailTemplateId = emailConnectionErrorTemplate,
                        emailTemplateName = "emailConnectionError",
                        data = dataMap,
                        clientEmailSettingId = clientSettingId
                )
                sendEmail(email)
            }

        }
    }

    fun sendNotificationConnectionErrorEmail(notificationError: NotificationError) {
        val clientSetting=getClientEmailSettings((clientSettingId)?:-1)
        notificationError.clientId?.let {clientId->
            val client = clientService.getClientByClientId(clientId)

            client?.let {

                val dataMap = mutableMapOf<String, Any>(
                        "name" to "${it.firstname} ${it.lastname}",
                        "error" to "${notificationError.status}"

                )

                val email = Email(
                        clientID = 1,
                        fromEmailAddress = InternetAddress(clientSetting),
                        toEmailAddresses = arrayOf(InternetAddress(it.email)),
                        emailTemplateId = emailConnectionErrorTemplate,
                        emailTemplateName = "emailConnectionError",
                        data = dataMap,
                        clientEmailSettingId = clientSettingId
                )
                sendEmail(email)
            }

        }
    }

    @Cacheable(key = "'client_1'+'setting_id_'+#id",cacheNames = ["client_email_settings"])
    private fun getClientEmailSettings(id:Long):String?{
        val clientSettings=clientSettingsEmailRepository.findById(id)
        if(!clientSettings.isPresent) throw CustomException("Email Settings not present for client 1")
        else return clientSettings.get().email
    }

    private fun toKafka(email: Email) {
        eventStream.clientEmailSend().send(MessageBuilder.withPayload(email).build())
    }

}