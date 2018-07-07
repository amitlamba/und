package com.und.service

import com.und.common.utils.encrypt
import com.und.factory.EmailServiceProviderConnectionFactory
import com.und.model.mongo.EmailStatus
import com.und.model.mongo.EmailStatusUpdate
import com.und.model.utils.Email
import com.und.model.utils.EmailSMTPConfig
import com.und.repository.mongo.EmailSentRepository
import com.und.utils.TenantProvider
import org.jsoup.Jsoup
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.regex.Pattern
import javax.mail.Message
import javax.mail.Session
import javax.mail.internet.MimeMessage

@Service
class EmailHelperService {

    @Autowired
    private lateinit var emailSentRepository: EmailSentRepository

    @Autowired
    private lateinit var templateContentCreationService: TemplateContentCreationService

    @Autowired
    private lateinit var emailServiceProviderConnectionFactory: EmailServiceProviderConnectionFactory

    @Value("\${und.url.event}")
    private lateinit var eventApiUrl: String

    val urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)"
    val trackingURL = "https://userndot.com/event/track"
    val excludeTrackingURLs = arrayOf(
            "^(https?|ftp)://(www.)?userndot.com.*\$"
    )

    fun createMimeMessage(session: Session, email: Email): MimeMessage {
        val emailSMTPConfig = emailServiceProviderConnectionFactory.getEmailServiceProvider(email.clientID)
        val msg = MimeMessage(session)
        msg.setFrom(email.fromEmailAddress)
        msg.setRecipients(Message.RecipientType.TO, email.toEmailAddresses)
        msg.setRecipients(Message.RecipientType.CC, email.ccEmailAddresses)
        msg.replyTo = email.replyToEmailAddresses
        msg.subject = email.emailSubject
        msg.setContent(email.emailBody, "text/html")
        if (emailSMTPConfig.CONFIGSET != null)
            msg.setHeader("X-SES-CONFIGURATION-SET", emailSMTPConfig.CONFIGSET)
        return msg
    }

    fun saveMailInMongo(email: Email, emailStatus: EmailStatus): String? { val mongoEmail: com.und.model.mongo.Email = com.und.model.mongo.Email(
                email.clientID,
                email.fromEmailAddress,
                email.toEmailAddresses,
                email.ccEmailAddresses,
                email.bccEmailAddresses,
                email.replyToEmailAddresses,
                email.emailSubject,
                email.emailBody!!,
                email.emailTemplateId,
                email.eventUser?.id,
                emailStatus = emailStatus
        )
        TenantProvider().setTenant(email.clientID.toString())
        val mmongoEmailPersisted: com.und.model.mongo.Email? = emailSentRepository.save(mongoEmail)

        return mmongoEmailPersisted?.let {
            val id = mmongoEmailPersisted.id
            val emailBody = mmongoEmailPersisted.emailBody
            val clientId = email.clientID
            if(id!=null) {
                templateContentCreationService.trackAllURLs(emailBody, clientId, id)
            }
            return id
        }

    }

    fun updateEmailStatus(mongoEmailId: String, emailStatus: EmailStatus, clientId: Long, clickTrackEventId: String? = null) {
        TenantProvider().setTenant(clientId.toString())
        val mongoEmail: com.und.model.mongo.Email = emailSentRepository.findById(mongoEmailId).get()
        if (mongoEmail.emailStatus.order < emailStatus.order) {
            mongoEmail.emailStatus = EmailStatus.READ
            mongoEmail.statusUpdates.add(EmailStatusUpdate(LocalDateTime.now(ZoneId.of("UTC")), emailStatus, clickTrackEventId))
            emailSentRepository.save(mongoEmail)
        }
    }

    fun updateSubjectAndBody(email: Email): Email {
        val emailToSend = email.copy()
        val model = emailToSend.data
        emailToSend.eventUser?.let {
            model["user"] = it
        }
        emailToSend.emailSubject = templateContentCreationService.getEmailSubject(emailToSend, model)
        emailToSend.emailBody = templateContentCreationService.getEmailBody(emailToSend, model)
        return emailToSend
    }

    fun session(clientId:Long, emailSMTPConfig:EmailSMTPConfig) = emailServiceProviderConnectionFactory.getSMTPSession(clientId, emailSMTPConfig)

    fun transport(clientId:Long) = emailServiceProviderConnectionFactory.getSMTPTransportConnection(clientId)

    fun closeTransport(clientId:Long) = emailServiceProviderConnectionFactory.closeSMTPTransportConnection(clientId)


    fun insertUnsubscribeLinkIfApplicable(content: String, unsubscribeLink: String, clientId: Int, mongoEmailId: String): String {
        val unsubscribePlaceholder: String = "##UND_UNSUBSCRIBE_LINK##"
        var qmark: String = if (unsubscribeLink.contains('?')) "&" else "?"
        return content.replace(unsubscribePlaceholder, "$unsubscribeLink$qmark"+"c=$clientId&e=$mongoEmailId")
    }

    fun getUnsubscribeLink(clientUnsubscribeLink: String, clientId: Int, mongoEmailId: String): String {
        var qmark: String = if (clientUnsubscribeLink.contains('?')) "&" else "?"
        return "$clientUnsubscribeLink$qmark"+"c=$clientId&e=$mongoEmailId"
    }

    fun addPixelTracking(content: String, clientId: Int, mongoEmailId: String): String {
        var doc = Jsoup.parse(content)
        val imageUrl = getImageUrl(clientId, mongoEmailId)
        doc.body().append("""<div><img src="$imageUrl"></div>""")
        return doc.body().html().toString()
    }

    fun getImageUrl(clientId: Int, mongoEmailId: String): String {
        val id = clientId.toString()+"###"+mongoEmailId
        return getImageUrl(id)
    }

    private fun getImageUrl(id: String): String {
        return "$eventApiUrl/email/image/${URLEncoder.encode(URLEncoder.encode(encrypt(id), "UTF-8"), "UTF-8")}.jpg"
    }

    fun trackAllURLs(content: String, clientId: Long, mongoEmailId: String): String {
        val containedUrls = ArrayList<String>()
        val pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE)
        val urlMatcher = pattern.matcher(content)

        while (urlMatcher.find()) {
            containedUrls.add(content.substring(urlMatcher.start(0),
                    urlMatcher.end(0)))
        }

        var replacedContent = content
        for(c in containedUrls) {
            var skip = false
            for(exclude in excludeTrackingURLs) {
                if (c.matches(exclude.toRegex())) {
                    skip = true
                    break
                }
            }
            if( skip )
                continue
            replacedContent = replacedContent.replace(c, "$trackingURL?c=$clientId&e=$mongoEmailId&u="+ URLEncoder.encode(c,"UTF-8"))
        }
        return replacedContent
    }
}