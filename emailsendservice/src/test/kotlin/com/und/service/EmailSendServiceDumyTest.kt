package com.und.service

import com.und.factory.Security
import com.und.model.utils.Email
import com.und.model.utils.EmailSMTPConfig
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.util.*
import javax.mail.*

import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

@RunWith(SpringRunner::class)
class EmailSendServiceDumyTest {


    lateinit var session: Session

    var emailSMTPConfig = EmailSMTPConfig(0L,
            1L,
            "smtp.gmail.com",
            465,
            "userndot19@gmail.com",
            "Userndot1@",
            Security.STARTTLS)

    var email = Email(3L,
            InternetAddress("userndot19@gmail.com"),
            arrayOf(InternetAddress("userndot19@gmail.com")),
            null,
            null,
            null,
            "testing",
            "Testing body",
            0L,
            "",
            mutableMapOf(),
            null,
            campaignId = 0L)


    @Before
    fun setUp() {
    }

    @Test
    fun sendEmailBySMTP() {

        session = testSession(email.clientID, emailSMTPConfig)

        val transport = testTransport(email.clientID)


        // Send the message.
        try {

            val msg = createMimeMessage(session, email)
            transport.sendMessage(msg, msg.allRecipients)
        } catch (ex: Exception) {

        } finally {
            // emailHelperService.closeTransport(email.clientID)
            transport.close()
        }
    }

    @Test
    fun sendEmailBySMTPNew() {

        session = createSMTPSession(emailSMTPConfig)

        val transport = session.transport
        if (!transport.isConnected) {
            transport.connect()
        }


        // Send the message.
        try {

            val msg = createMimeMessage(session, email)
            transport.sendMessage(msg, msg.allRecipients)
        } catch (ex: Exception) {

        } finally {
            // emailHelperService.closeTransport(email.clientID)
            transport.close()
        }
    }


    fun createMimeMessage(session: Session, email: Email): Message {
        //val emailSMTPConfig = emailServiceProviderConnectionFactory.getEmailServiceProvider(email.clientID)
        val msg = MimeMessage(session)
        msg.setFrom(email.fromEmailAddress)
        msg.setRecipients(Message.RecipientType.TO, email.toEmailAddresses)
        msg.setRecipients(Message.RecipientType.CC, email.ccEmailAddresses)
        msg.replyTo = email.replyToEmailAddresses
        msg.subject = email.emailSubject
        msg.setContent(email.emailBody, "text/html")
//        if (emailSMTPConfig.CONFIGSET != null)
//            msg.setHeader("X-SES-CONFIGURATION-SET", emailSMTPConfig.CONFIGSET)
        return msg
    }

    private fun testSession(clientID: Long, emailSMTPConfig: EmailSMTPConfig): Session {

        session = createSMTPSession(emailSMTPConfig.PORT)

        return session
    }


    fun createSMTPSession(port: Int): Session {
        val props = Properties()
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", 465);
        props.put("mail.smtp.auth", true);
        props.put("mail.smtp.starttls.required",true);
        props.put("mail.smtp.starttls.enable", true);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.port", 465);
//	        //sslException
        props.put("mail.smtp.quitwait", false)

        // Create a Session object to represent a mail session with the specified properties.
        return Session.getDefaultInstance(props)

    }

    private fun testTransport(clientId: Long): Transport {
        val transport = session.transport
        transport.connect(emailSMTPConfig.HOST, emailSMTPConfig.SMTP_USERNAME, emailSMTPConfig.SMTP_PASSWORD)
        return transport
    }


    private fun createSMTPSession(emailSmtpConfig: EmailSMTPConfig): Session {
        val props = Properties()

        props["mail.transport.protocol"] = "smtp"
        props["mail.smtp.host"] = emailSmtpConfig.HOST
        props["mail.smtp.port"] = emailSmtpConfig.PORT
        props["mail.smtp.auth"] = true
        props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"

        when (emailSmtpConfig.security) {
            Security.SSL, Security.TLS -> {
                props["mail.smtp.ssl.enable"] = true
                props["mail.smtp.starttls.enable"] = false
            }
            Security.STARTTLS -> {
                props["mail.smtp.ssl.enable"] = false
                props["mail.smtp.starttls.enable"] = true
            }
            Security.NONE -> {
                props["mail.smtp.ssl.enable"] = false
                props["mail.smtp.starttls.enable"] = false
            }
        }


        val authenticator: Authenticator = object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(emailSmtpConfig.SMTP_USERNAME, emailSmtpConfig.SMTP_PASSWORD)
            }
        }
        return Session.getInstance(props, authenticator)
    }
}

