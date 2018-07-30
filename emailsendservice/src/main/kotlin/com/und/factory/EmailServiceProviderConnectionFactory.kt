package com.und.factory

import com.und.model.utils.EmailSMTPConfig
import com.und.service.ServiceProviderCredentialsService
import com.und.utils.loggerFor
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import javax.mail.Session
import javax.mail.Transport

@Component
class EmailServiceProviderConnectionFactory {

    companion object {
        val logger: Logger = loggerFor(EmailServiceProviderConnectionFactory::class.java)
    }


    @Autowired
    lateinit var serviceProviderCredentialsService: ServiceProviderCredentialsService

    var emailSMPTConfigs: ConcurrentHashMap<Long, EmailSMTPConfig> = ConcurrentHashMap()
    var emailSMTPSessions: ConcurrentHashMap<Long, Session> = ConcurrentHashMap()
    var emailSMTPTransportConnections: ConcurrentHashMap<Long, Transport> = ConcurrentHashMap()

    fun getEmailServiceProvider(clientID: Long): EmailSMTPConfig {
        return when {
            emailSMPTConfigs.containsKey(clientID) -> emailSMPTConfigs[clientID] as EmailSMTPConfig
            else -> {
                synchronized(clientID) {
                    val serviceProviderCreds = serviceProviderCredentialsService.findActiveEmailServiceProvider(clientID)
                    val wspCreds = serviceProviderCredentialsService.buildWebServiceProviderCredentials(serviceProviderCreds)
                    val emailSMTPConfig = EmailSMTPConfig(
                            serviceProviderCredentialsId = wspCreds.id,
                            clientID = clientID,
                            HOST = wspCreds.credentialsMap["url"]!!,
                            PORT = wspCreds.credentialsMap["port"]!!.toInt(),
                            SMTP_USERNAME = wspCreds.credentialsMap["username"]!!,
                            SMTP_PASSWORD = wspCreds.credentialsMap["password"]!!
                    )
                    emailSMPTConfigs[clientID] = emailSMTPConfig
                    emailSMTPConfig
                }

            }
        }

    }

    fun getSMTPSession(clientID: Long, emailSMTPConfig: EmailSMTPConfig): Session {

        return when {
            emailSMTPSessions.containsKey(clientID) -> emailSMTPSessions[clientID] as Session
            else -> {
                synchronized(clientID) {
                    emailSMPTConfigs[clientID] = emailSMTPConfig
                    val session = createSMTPSession(emailSMTPConfig.PORT)
                    emailSMTPSessions[clientID] = session
                    session
                }
            }

        }
    }

    fun getSMTPTransportConnection(clientID: Long): Transport {

        return when {
            emailSMTPTransportConnections.containsKey(clientID) -> emailSMTPTransportConnections[clientID] as Transport
            else -> {
                synchronized(clientID) {
                    val emailSMTPConfig = getEmailServiceProvider(clientID)
                    val transport = getSMTPSession(clientID, emailSMTPConfig).transport
                    val esp = getEmailServiceProvider(clientID)
                    logger.debug("Email Service Provider: $esp")
                    transport.connect(esp.HOST, esp.SMTP_USERNAME, esp.SMTP_PASSWORD)
                    emailSMTPTransportConnections[clientID] = transport
                    transport
                }
            }
        }

    }

    fun closeSMTPTransportConnection(clientID: Long) {
        synchronized(clientID) {
            if (emailSMTPTransportConnections.containsKey(clientID)) {
                try {
                    emailSMTPTransportConnections[clientID]!!.close()
                } finally {
                    emailSMTPTransportConnections.remove(clientID)
                }
            }
        }
    }

    private fun createSMTPSession(port: Int): Session {
        // Create a Properties object to contain connection configuration information.
        val props = System.getProperties()
        props["mail.transport.protocol"] = "smtp"
        props["mail.smtp.port"] = port
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.socketFactory.class"]="javax.net.ssl.SSLSocketFactory"
        props["mail.smtp.socketFactory.port"]=port

        //props["mail.smtp.quitwait"]=false
        // Create a Session object to represent a mail session with the specified properties.
        return Session.getDefaultInstance(props)
    }
}