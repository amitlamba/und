package com.und.service

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import com.amazonaws.services.simpleemail.model.Body
import com.amazonaws.services.simpleemail.model.Content
import com.amazonaws.services.simpleemail.model.Destination
import com.amazonaws.services.simpleemail.model.SendEmailRequest
import com.und.model.mongo.EmailStatus.NOT_SENT
import com.und.model.utils.Email
import com.und.model.utils.EmailSESConfig
import com.und.model.utils.EmailSMTPConfig
import com.und.utils.loggerFor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import com.amazonaws.services.simpleemail.model.Message as SESMessage
import org.jsoup.Jsoup


@Service
class EmailSendService {
    companion object {
        protected val logger = loggerFor(EmailSendService::class.java)
    }

    @Autowired
    private lateinit var emailHelperService: EmailHelperService

    fun sendEmailByAWSSDK(emailSESConfig: EmailSESConfig, email: Email) {
        val credentialsProvider: AWSCredentialsProvider = AWSStaticCredentialsProvider(BasicAWSCredentials(emailSESConfig.awsAccessKeyId, emailSESConfig.awsSecretAccessKey))
        try {
            val client = AmazonSimpleEmailServiceClientBuilder.standard()
                    .withCredentials(credentialsProvider)
                    .withRegion(emailSESConfig.region).build()
            val request = SendEmailRequest()
            with(request) {
                destination = Destination().withToAddresses(
                        (email.toEmailAddresses.map { it.address }.toMutableList()))

                message = (SESMessage()
                        .withBody(Body()
                                .withHtml(Content()
                                        .withCharset("UTF-8").withData(email.emailBody))
                        )
                        .withSubject(Content()
                                .withCharset("UTF-8").withData(email.emailSubject)))

                source = email.fromEmailAddress?.address

            }
            client.sendEmail(request)
            logger.debug("Email sent!")
        } catch (ex: Exception) {
            logger.error("The email was not sent. Error message: " + ex.message)
        }
    }

    fun sendEmailBySMTP(emailSMTPConfig: EmailSMTPConfig, email: Email) {



        // Send the message.
        try {
            val session = emailHelperService.session(email.clientID, emailSMTPConfig)
            val transport = session.transport
            if(!transport.isConnected) { transport.connect()}


            logger.debug("Sending...")
            val msg = emailHelperService.createMimeMessage(session, email)
            logger.info("email sent to   ${email.toEmailAddresses} from  ${email.fromEmailAddress}} with msg ${email.emailBody}")
            transport.sendMessage(msg, msg.allRecipients)
        } catch (ex: Exception) {
            logger.error("The email was not sent.")
            logger.error("Error message: " + ex.message)
        } finally {
            //emailHelperService.closeTransport(email.clientID)
        }

    }






}
