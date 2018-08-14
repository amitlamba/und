package com.und.service

import com.und.common.utils.loggerFor
import com.und.config.EventStream
import com.und.model.jpa.ContactUs
import com.und.model.jpa.security.Client
import com.und.model.redis.security.UserCache
import com.und.model.utils.Email
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import javax.mail.internet.InternetAddress

@Service
class EmailService {

    companion object {

        protected val logger = loggerFor(EmailService::class.java)
        const val forgotPasswordTemplate = 1L
        const val verificationTemplate = 2L
        const val contactusTemplate = 3L
        const val supportTemplate=4L
    }

    @Value("\${und.auth-url}")
    lateinit var authUrl: String

    @Value("\${und.url.clientpanelui}")
    lateinit var clientPanelUrl: String

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
                "name" to "${code.firstname} ${code.lastname}",
                "resetPasswordLink" to "${clientPanelUrl}/resetpwd/${code.pswrdRstKey}"
//                "resetPasswordLink" to "${authUrl}/register/resetpassword/${code.pswrdRstKey}"
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

    fun sendContactUserDetails(contactInfo: ContactUs){
        val dataMap = mutableMapOf<String, Any>(
                "name" to contactInfo.name,
                "email" to contactInfo.email,
                "companyname" to contactInfo.companyName,
                "message" to contactInfo.message,
                "mobile" to contactInfo.mobileNo
        )

        val email = Email(
                clientID = 1,
                toEmailAddresses = arrayOf(InternetAddress("support@userndot.com")),
                emailTemplateId = EmailService.supportTemplate,
                emailTemplateName = "support",
                data = dataMap

        )
        sendEmail(email)
    }
    fun sendContactUsEmail(contactInfo: ContactUs) {
        sendContactUserDetails(contactInfo)
        val dataMap = mutableMapOf<String, Any>(
                "name" to "${contactInfo.name}"
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
                "name" to "${client.firstname} ${client.lastname}",
                "emailVerificationLink" to "${authUrl}/register/verifyemail/${client.email}/${client.clientVerification.emailCode}"
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