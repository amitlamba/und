package com.und.service

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import com.amazonaws.services.simpleemail.model.*
import com.und.exception.EmailError
import com.und.exception.EmailFailureException
import com.und.model.utils.Email
import com.und.model.utils.EmailSESConfig
import com.und.model.utils.EmailSMTPConfig
import com.und.utils.loggerFor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.mail.Address
import javax.mail.AuthenticationFailedException
import javax.mail.MessagingException
import javax.mail.SendFailedException
import javax.mail.event.TransportEvent
import javax.mail.event.TransportListener
import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress
import com.amazonaws.services.simpleemail.model.Message as SESMessage


@Service
class EmailSendService {
    companion object {
        protected val logger = loggerFor(EmailSendService::class.java)
    }

    @Autowired
    private lateinit var emailHelperService: EmailHelperService

    private val messageTransPortListener = MessageTransPortListener()

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
        } catch (e: MessageRejectedException) {
            logger.error(e.message)
            val error = EmailError()
            with(error) {
                clientid = email.clientID
                failureType = EmailError.FailureType.OTHER
                causeMessage = "Message rejected : ${e.message}"
                failedSettingId = emailSESConfig.serviceProviderCredentialsId

            }
            throw  EmailFailureException("Message rejected : ${e.message}", e, error)

        } catch (e: MailFromDomainNotVerifiedException) {
            e.errorType
            e.errorCode
            logger.error(e.message)
            val error = EmailError()
            with(error) {
                clientid = email.clientID
                failureType = EmailError.FailureType.CONNECTION
                causeMessage = "From Address Not Verified : ${e.message}"
                failedSettingId = emailSESConfig.serviceProviderCredentialsId
                from = email.fromEmailAddress?.toString()
            }
            throw  EmailFailureException("From Address Not Verified : ${e.message}", e, error)

        } catch (e: ConfigurationSetDoesNotExistException) {
            logger.error(e.message)
            val error = EmailError()
            with(error) {
                clientid = email.clientID
                failureType = EmailError.FailureType.CONNECTION
                causeMessage = "Configuration set ${e.configurationSetName} doesn't exist : ${e.message}"
                failedSettingId = emailSESConfig.serviceProviderCredentialsId

            }
            throw  EmailFailureException("Configuration set ${e.configurationSetName} doesn't exist : ${e.message}", e, error)

        } catch (e: Exception) {
            logger.error("The email was not sent. Error message: ${e.message}")
            val error = EmailError()
            with(error) {
                clientid = email.clientID
                failureType = EmailError.FailureType.OTHER
                causeMessage = "Unable to send email : ${e.message}"
                failedSettingId = emailSESConfig.serviceProviderCredentialsId
            }
            throw  EmailFailureException("Unable to send email : ${e.message}", e, error)

        }
    }

    fun sendEmailBySMTP(emailSMTPConfig: EmailSMTPConfig, email: Email) {
        // Send the message.
        try {
            val session = emailHelperService.session(email.clientID, emailSMTPConfig)

            val transport = emailHelperService.transport(email.clientID)
            transport.removeTransportListener(messageTransPortListener)
            //transport.addTransportListener(messageTransPortListener)
            //transport.
            if (!transport.isConnected) {
                try {
                    transport.connect()
                } catch (e: AuthenticationFailedException) {
                    logger.error(e.message)

                    val error = EmailError()
                    with(error) {
                        clientid = email.clientID
                        failureType = EmailError.FailureType.CONNECTION
                        causeMessage = "Incorrect Authentication : ${e.message}"
                    }
                    throw  EmailFailureException("Incorrect Authentication : ${e.message}", e, error)
                }

            }


            logger.debug("Sending...")
            val msg = emailHelperService.createMimeMessage(session, email)
            logger.info("email sent to   ${email.toEmailAddresses} from  ${email.fromEmailAddress}} with msg ${email.emailBody}")
            //FIXME transport is single thread use thread safety synchronization
            transport.sendMessage(msg, msg.allRecipients)
        } catch (e: SendFailedException) {

            logger.error(e.message)
            val error = EmailError()
            with(error) {
                clientid = email.clientID
                failureType = EmailError.FailureType.DELIVERY
                causeMessage = "Incorrect Authentication : ${e.message}"
                failedSettingId = emailSMTPConfig.serviceProviderCredentialsId
                invalidAddresses = e.invalidAddresses.filter { it is InternetAddress }.map { address -> (address as InternetAddress).address }
                validSentAddresses = e.validSentAddresses.filter { it is InternetAddress }.map { address -> (address as InternetAddress).address }
                validUnsentAddresses = e.validUnsentAddresses.filter { it is InternetAddress }.map { address -> (address as InternetAddress).address }

            }
            throw  EmailFailureException("Failure while4 sending emails : ${e.message}", e, error)


        } catch (e: AddressException) {
            val invalidFormattedAddress = e.ref
            logger.error(e.message)

            val error = EmailError()
            with(error) {
                clientid = email.clientID
                failureType = EmailError.FailureType.INCORRECT_EMAIL
                causeMessage = "Incorrect email address : ${e.message}"
                failedSettingId = emailSMTPConfig.serviceProviderCredentialsId
                invalidAddresses = listOf(invalidFormattedAddress)
            }
            throw  EmailFailureException("Incorrect email address : ${e.message}", e, error)

        } catch (e: MessagingException) {
            logger.error(e.message)
            val error = EmailError()
            with(error) {
                clientid = email.clientID
                failureType = EmailError.FailureType.OTHER
                causeMessage = "Unable to send email : ${e.message}"
                failedSettingId = emailSMTPConfig.serviceProviderCredentialsId
            }
            throw  EmailFailureException("Unable to send email : ${e.message}", e, error)

        } catch (e: Exception) {
            logger.error(e.message)
            val error = EmailError()
            with(error) {
                clientid = email.clientID
                failureType = EmailError.FailureType.OTHER
                causeMessage = "Unable to send email : ${e.message}"
                failedSettingId = emailSMTPConfig.serviceProviderCredentialsId
            }
            throw  EmailFailureException("Unable to send email : ${e.message}", e, error)
        } finally {
            //emailHelperService.closeTransport(email.clientID)
        }

    }

    class MessageTransPortListener : TransportListener {

        private fun handleBounceEmails(invalidAddresses: Array<Address>) {
            invalidAddresses.filter { it is InternetAddress }.map { address ->

                (address as InternetAddress).address

            }.forEach {

                //send to failure count and blacklist if required
            }

        }

        override fun messageNotDelivered(e: TransportEvent) {
            e.validUnsentAddresses?.let { invalidAddresses -> handleBounceEmails(invalidAddresses) }
            e.invalidAddresses?.let { invalidAddresses -> handleBounceEmails(invalidAddresses) }
        }

        override fun messageDelivered(e: TransportEvent) {


        }

        override fun messagePartiallyDelivered(e: TransportEvent) {
            e.validUnsentAddresses?.let { invalidAddresses -> handleBounceEmails(invalidAddresses) }
            e.invalidAddresses?.let { invalidAddresses -> handleBounceEmails(invalidAddresses) }

        }
    }


}
