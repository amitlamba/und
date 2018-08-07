package com.und.service

import com.und.config.EventStream
import com.und.model.utils.Email
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.messaging.support.MessageBuilder
import org.springframework.test.context.junit4.SpringRunner
import javax.mail.internet.InternetAddress

@RunWith(SpringRunner::class)
@SpringBootTest
class KafkaProducerTest {

    val FROM = "amit@userndot.com"
    val FROMNAME = "Amit from Userndot"

    // Replace recipient@example.com with a "To" address. If your account
// is still in the sandbox, this address must be verified.
    val TO = "amitlamba4198@gmail.com"

    // Replace smtp_username with your Amazon SES SMTP user name.
    val SMTP_USERNAME = "smtp_username"

    // Replace smtp_password with your Amazon SES SMTP password.
    val SMTP_PASSWORD = "smtp_password"

    // The name of the Configuration Set to use for this message.
// If you comment out or remove this variable, you will also need to
// comment out or remove the header below.
    val CONFIGSET = "ConfigSet"

    // Amazon SES SMTP host name. This example uses the US West (Oregon) Region.
    val HOST = "email-smtp.us-west-2.amazonaws.com"

    // The port you will connect to on the Amazon SES SMTP endpoint.
    val PORT = 587

    val SUBJECT = "Amazon SES test (SMTP interface accessed using Java)"

    val BODY = arrayOf(
            "<h1>Amazon SES SMTP Email Test</h1>",
            "<p>This email was sent with Amazon SES using the ",
            "<a href='https://github.com/javaee/javamail'>Javamail Package</a>",
            " for <a href='https://www.java.com'>Java</a>.").joinToString(
            System.getProperty("line.separator"))

    @Autowired
    private lateinit var eventStream: EventStream

    @Test
    fun toKafkaEmail() {
        val email: Email = Email(clientID = 2,
                fromEmailAddress = InternetAddress(FROM, FROMNAME),
                toEmailAddresses = arrayOf(InternetAddress(TO)),
                emailSubject = SUBJECT,
                emailBody = BODY,
                emailTemplateId = 0L,
                emailTemplateName = "",
                campaignId = 0
                )
        eventStream.emailEventSend().send(MessageBuilder.withPayload(email).build())
    }
}