package com.und.email.factory

import com.und.exception.EmailFailureException
import com.und.model.utils.EmailSMTPConfig
import com.und.email.service.EmailHelperService
import com.und.service.ServiceProviderCredentialsService
import com.und.utils.loggerFor
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport

@Component
class EmailServiceProviderConnectionFactory {

    companion object {
        val logger: Logger = loggerFor(EmailServiceProviderConnectionFactory::class.java)
    }


    @Autowired
    lateinit var serviceProviderCredentialsService: ServiceProviderCredentialsService

    @Autowired
    private lateinit var emailHelperService: EmailHelperService

    var emailSMPTConfigs: ConcurrentHashMap<String, EmailSMTPConfig> = ConcurrentHashMap()
    var emailSMTPSessions: ConcurrentHashMap<String, Session> = ConcurrentHashMap()
    var emailSMTPTransportConnections: ConcurrentHashMap<String, Transport> = ConcurrentHashMap()

    fun getEmailServiceProvider(clientID: Long,clientEmailSettingId: Long): EmailSMTPConfig {
        return when {
            emailSMPTConfigs.containsKey("${clientID}${clientEmailSettingId}") -> emailSMPTConfigs["${clientID}${clientEmailSettingId}"] as EmailSMTPConfig
            else -> {
                synchronized(clientID) {
                    val serviceProviderCreds = emailHelperService.getEmailServiceProviderCredentials(clientID,clientEmailSettingId)
                    val wspCreds = serviceProviderCredentialsService.buildWebServiceProviderCredentials(serviceProviderCreds)
                    val emailSMTPConfig = EmailSMTPConfig(
                            serviceProviderCredentialsId = wspCreds.id,
                            clientID = clientID,
                            HOST = wspCreds.credentialsMap["url"]
                                    ?: throw EmailFailureException("Host is not provided"),
                            PORT = wspCreds.credentialsMap["port"]?.toInt()
                                    ?: throw EmailFailureException("Port is not provided"),
                            SMTP_USERNAME = wspCreds.credentialsMap["username"]
                                    ?: throw EmailFailureException("Username is not provided"),
                            SMTP_PASSWORD = wspCreds.credentialsMap["password"]
                                    ?: throw EmailFailureException("Password is not provided"),
                            security = wspCreds.credentialsMap["security"]?.let { security -> Security.valueOf(security) }
                                    ?: Security.STARTTLS

                    )
                    emailSMPTConfigs["${clientID}${clientEmailSettingId}"] = emailSMTPConfig
                    emailSMTPConfig
                }

            }
        }

    }

    fun getSMTPSession(clientID: Long, emailSMTPConfig: EmailSMTPConfig,clientEmailSettingId: Long): Session {

        return when {
            emailSMTPSessions.containsKey("${clientID}${clientEmailSettingId}") -> emailSMTPSessions["${clientID}${clientEmailSettingId}"] as Session
            else -> {
                synchronized(clientID) {
                    emailSMPTConfigs["${clientID}${clientEmailSettingId}"] = emailSMTPConfig
                    val session = createSMTPSession(emailSMTPConfig)
                    emailSMTPSessions["${clientID}${clientEmailSettingId}"] = session
                    session
                }
            }

        }
    }

    fun getSMTPTransportConnection(clientID: Long,clientEmailSettingId:Long): Transport {

        return when {
            emailSMTPTransportConnections.containsKey("${clientID}${clientEmailSettingId}") -> emailSMTPTransportConnections["${clientID}${clientEmailSettingId}"] as Transport
            else -> {
                synchronized(clientID) {
                    val emailSMTPConfig = getEmailServiceProvider(clientID,clientEmailSettingId)
                    val transport = getSMTPSession(clientID, emailSMTPConfig,clientEmailSettingId).transport
                    val esp = getEmailServiceProvider(clientID,clientEmailSettingId)
                    logger.debug("Email Service Provider: $esp")
                    emailSMTPTransportConnections["${clientID}${clientEmailSettingId}"] = transport
                    transport
                }
            }
        }

    }


    fun closeSMTPTransportConnection(clientID: Long,clientEmailSettingId: Long) {
        synchronized(clientID) {
            if (emailSMTPTransportConnections.containsKey("${clientID}${clientEmailSettingId}")) {
                try {
                    emailSMTPTransportConnections["${clientID}${clientEmailSettingId}"]?.close()
                } finally {
                    emailSMTPTransportConnections.remove("${clientID}${clientEmailSettingId}")
                }
            }
        }
    }

    private fun createSMTPSession(emailSmtpConfig: EmailSMTPConfig): Session {
        val props = Properties()

        props["mail.transport.protocol"] = "smtp"
        props["mail.smtp.auth"] = true
        props["mail.smtp.host"] = emailSmtpConfig.HOST
        props["mail.smtp.port"] = emailSmtpConfig.PORT
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


        val authenticator
                : Authenticator
        ? = object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(emailSmtpConfig.SMTP_USERNAME, emailSmtpConfig.SMTP_PASSWORD)
            }
        }
        return Session.getInstance(props, authenticator)
    }
}

enum class Security {
    SSL, TLS, STARTTLS, NONE
}