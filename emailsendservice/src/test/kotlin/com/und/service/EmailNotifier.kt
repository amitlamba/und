/**
 * Copyright 2018 UsernDot.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.und.service


import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

/**
 * Notifier for sending email notifications. This uses the JavaMail api to send emails.
 *
 * @see [JavaMail API](https://java.net/projects/javamail/pages/Home)
 */
class EmailNotifier {

    var defaultValues: Map<String, String> = mapOf()
    var defaultProperties: Properties = Properties()
    private val msgNotificationMap = ConcurrentHashMap<Message, String>()
    private var msgFields: Map<String, String>? = null
    private var emailSession: Session? = null
    private var emailTransport: Transport? = null


    /**
     * A wrapper class to hold a key and its default value
     */
    private class Field internal constructor(internal val key: String, internal val defaultVal: String)

    fun open(defaultValues: Map<String, String>) {
        LOG.debug("EmailNotifier open called with context {}", defaultValues)
        val defaultFieldValues = HashMap<String, Any>()
        defaultFieldValues.putAll(defaultValues)
        this.msgFields = getMsgFields(defaultFieldValues, null)
        this.emailSession = getEmailSession(defaultProperties)
        this.emailTransport = getEmailTransport(emailSession!!)
    }


    fun notify(notification: Email) {
        // merge fieldsAndValues with msgFields
        val fieldsToSend = getMsgFields(defaultValues, this.msgFields)

        // validate fieldsToSend
        for (field in MSG_FIELDS) {
            val `val` = fieldsToSend[field.key]
            if (`val` == null || `val`.isEmpty()) {
                throw Exception("Field '" + field.key + "' is empty")
            }
        }

        try {
            val emailMessage = getEmailMessage(fieldsToSend)
            if (!emailTransport!!.isConnected) {
                emailTransport!!.connect()
            }
            emailTransport!!.sendMessage(emailMessage, emailMessage.allRecipients)
        } catch (ex: MessagingException) {
            LOG.error("Got exception", ex)
            throw Exception(ex)
        }

    }


    /**
     * Returns a new map containing the values for email message fields from the first map,
     * using values from second map as defaults.
     */
    private fun getMsgFields(values: Map<String, Any>, defaults: Map<String, String>?): Map<String, String> {
        val fields = mutableMapOf<String, String>()
        for (field in MSG_FIELDS) {
            val value = values[field.key] as String?
            fields[field.key] = value ?: if (defaults != null) defaults[field.key] ?: "" else field.defaultVal
        }
        return fields
    }


    private fun getProperty(properties: Properties, field: Field): String {
        return properties.getProperty(field.key, field.defaultVal)
    }

    /**
     * Return a [Session] object initialized with the values
     * from the passed in properties.
     */
    private fun getEmailSession(properties: Properties): Session {
        val sessionProps = Properties()
        sessionProps[SMTP_HOST] = getProperty(properties, PROP_HOST)
        sessionProps[SMTP_PORT] = getProperty(properties, PROP_PORT)
        sessionProps[SMTP_SSL_ENABLE] = getProperty(properties, PROP_SSL)
        sessionProps[SMTP_STARTTLS_ENABLE] = getProperty(properties, PROP_STARTTLS)
        sessionProps[MAIL_TRANSPORT_PROTOCOL] = getProperty(properties, PROP_PROTOCOL)
        // init authenticator
        val userName = getProperty(properties, PROP_USERNAME)
        val password = getProperty(properties, PROP_PASSWORD)
        var authenticator: Authenticator? = null
        if (!userName.isEmpty() && !password.isEmpty()) {
            sessionProps[SMTP_AUTH] = getProperty(properties, PROP_AUTH)
            authenticator = object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(userName, password)
                }
            }
        }
        val debug = java.lang.Boolean.parseBoolean(getProperty(properties, PROP_DEBUG))
        // create session
        LOG.debug("Creating session with properties {}, debug {}", sessionProps, debug)
        val session = Session.getInstance(sessionProps, authenticator)
        session.debug = debug
        return session
    }

    /**
     * Return a [Transport] object from the session registering the passed in transport listener
     * for delivery notifications.
     */
    private fun getEmailTransport(session: Session): Transport {
        try {
            val transport = session.transport
            if (!transport.isConnected) {
                transport.connect()
            }
            return transport
        } catch (ex: MessagingException) {
            LOG.error("Got exception while initializing transport", ex)
            throw Exception("Got exception while initializing transport", ex)
        }

    }

    /**
     * Construct a [Message] from the map of message field values
     */
    @Throws(MessagingException::class)
    private fun getEmailMessage(fields: Map<String, String>): Message {
        val msg = MimeMessage(emailSession)
        msg.setFrom(InternetAddress(fields[FIELD_FROM.key]))
        val address = arrayOf(InternetAddress(fields[FIELD_TO.key]))
        msg.setRecipients(Message.RecipientType.TO, address)
        msg.subject = fields[FIELD_SUBJECT.key]
        msg.sentDate = Date()
        val content = MimeMultipart()
        val mimeBodyPart = MimeBodyPart()
        mimeBodyPart.setContent(fields[FIELD_BODY.key], fields[FIELD_CONTENT_TYPE.key])
        content.addBodyPart(mimeBodyPart)
        msg.setContent(content)
        return msg
    }


    companion object {
        private val LOG = LoggerFactory.getLogger(EmailNotifier::class.java)

        private fun field(key: String, defaultVal: String): Field {
            return Field(key, defaultVal)
        }

        // configuration properties
        private val PROP_USERNAME = field("username", "")
        private val PROP_PASSWORD = field("password", "")
        private val PROP_HOST = field("host", "localhost")
        private val PROP_PORT = field("port", "25")
        private val PROP_SSL = field("ssl", "false")
        private val PROP_STARTTLS = field("starttls", "false")
        private val PROP_DEBUG = field("debug", "false")
        private val PROP_PROTOCOL = field("protocol", "smtp")
        private val PROP_AUTH = field("auth", "true")

        // SMTP keys
        private val SMTP_HOST = "mail.smtp.host"
        private val SMTP_PORT = "mail.smtp.port"
        private val SMTP_AUTH = "mail.smtp.auth"
        private val SMTP_SSL_ENABLE = "mail.smtp.ssl.enable"
        private val SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable"
        private val MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol"


        // Message fields
        private val FIELD_FROM = field("from", "admin@localhost")
        private val FIELD_TO = field("to", "")
        private val FIELD_SUBJECT = field("subject", "Alert")
        private val FIELD_CONTENT_TYPE = field("contentType", "text/plain")
        private val FIELD_BODY = field("body", "Got an alert")
        private val MSG_FIELDS = arrayOf(FIELD_FROM, FIELD_TO, FIELD_SUBJECT, FIELD_CONTENT_TYPE, FIELD_BODY)
    }

}
