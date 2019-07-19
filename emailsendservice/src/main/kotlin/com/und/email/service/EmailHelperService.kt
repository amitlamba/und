package com.und.email.service

import com.und.email.factory.EmailServiceProviderConnectionFactory
import com.und.model.mongo.EmailStatus
import com.und.model.utils.Email
import com.und.model.utils.EmailSMTPConfig
import com.und.model.jpa.ServiceProviderCredentials
import com.und.email.repository.jpa.ClientEmailSettingsRepository
import com.und.email.repository.jpa.ServiceProviderCredentialsRepository
import com.und.email.repository.mongo.EmailSentRepository
import com.und.service.ServiceProviderCredentialsService
import com.und.service.TemplateContentCreationService
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import java.net.URLEncoder
import javax.mail.Session
import javax.mail.internet.MimeMessage
import com.und.model.mongo.Email as MongoEmail

@Service
class EmailHelperService {

    @Autowired
    private lateinit var emailSentRepository: EmailSentRepository

    @Autowired
    private lateinit var templateContentCreationService: TemplateContentCreationService

    @Autowired
    private lateinit var emailServiceProviderConnectionFactory: EmailServiceProviderConnectionFactory

    @Autowired
    private lateinit var clientEmailSettingsRepository: ClientEmailSettingsRepository

    @Autowired
    lateinit var serviceProviderCredentialsRepository: ServiceProviderCredentialsRepository

//    @Autowired
//    private lateinit var serviceProviderCredentialsService: ServiceProviderCredentialsService

    @Value("\${und.url.event}")
    private lateinit var eventApiUrl: String

    companion object {
        var logger=LoggerFactory.getLogger(EmailHelperService::class.java)
    }


    fun createMimeMessage(session: Session, email: Email): MimeMessage {
        val emailSMTPConfig = emailServiceProviderConnectionFactory.getEmailServiceProvider(email.clientID,email.clientEmailSettingId!!)

        val msg = MimeMessage(session)

        val messageBuildHelper = MimeMessageHelper(msg)

        messageBuildHelper.setTo(email.toEmailAddresses)
        if (email.ccEmailAddresses != null) messageBuildHelper.setCc(email.ccEmailAddresses)
        messageBuildHelper.setFrom(email.fromEmailAddress)
        val replyTo = email.replyToEmailAddresses
        if (replyTo != null && replyTo.size == 1) {
            messageBuildHelper.setReplyTo(replyTo.first())
        } else {
            msg.replyTo = email.replyToEmailAddresses
        }
        messageBuildHelper.setSubject(email.emailSubject)
        messageBuildHelper.setText(email.emailBody, true)
        if (emailSMTPConfig.CONFIGSET != null)
            msg.setHeader("X-SES-CONFIGURATION-SET", emailSMTPConfig.CONFIGSET)
        return msg
    }

    fun saveMailInMongo(email: Email, emailStatus: EmailStatus, mongoEmailId: String? = null) {
        val mongoEmail = MongoEmail(
                id = mongoEmailId,
                clientID = email.clientID,
                fromEmailAddress = email.fromEmailAddress,
                toEmailAddresses = email.toEmailAddresses,
                emailTemplateId = email.emailTemplateId,
                emailSubject = email.emailSubject ?: "NA",
                campaignId = email.campaignId?:-1,  // campaign id is never null because we save in mongo only when campaign id is not null.
                status = emailStatus,
                userID = email.eventUser?.id,
                segmentId = email.segmentId

        )
        //TenantProvider().setTenant(email.clientID.toString())
        mongoEmailId
                ?: throw IllegalStateException("couldn't save email data for tracking of campaign id ${email.campaignId} sending email to ${email.toEmailAddresses}")
        emailSentRepository.saveEmail(mongoEmail, clientId = email.clientID)
    }


    fun updateEmailStatus(mongoEmailId: String, emailStatus: EmailStatus, clientId: Long, clickTrackEventId: String? = null) {
        //TenantProvider().setTenant(clientId.toString())
        //val mongoEmail = emailSentRepository.findById(mongoEmailId).get()
        //if (mongoEmail.emailStatus.order < emailStatus.order) {
           // mongoEmail.emailStatus = EmailStatus.READ
           // mongoEmail.statusUpdates.add(EmailStatusUpdate(LocalDateTime.now(ZoneId.of("UTC")), emailStatus, clickTrackEventId))
            emailSentRepository.updateStatus(mongoEmailId,emailStatus, clientId, clickTrackEventId)
       // }
    }

    fun subjectAndBody(email: Email): Pair<String, String> {
        val emailToSend = email.copy()
        val model = emailToSend.data

        val subject = templateContentCreationService.getEmailSubject(emailToSend, model)
        val body = templateContentCreationService.getEmailBody(emailToSend, model)
        return Pair(subject, body)
    }

    fun session(clientId: Long, emailSMTPConfig: EmailSMTPConfig,clientEmailSettingId: Long) = emailServiceProviderConnectionFactory.getSMTPSession(clientId, emailSMTPConfig,clientEmailSettingId)

    fun transport(clientId: Long,clientEmailSettingId: Long) = emailServiceProviderConnectionFactory.getSMTPTransportConnection(clientId,clientEmailSettingId)

    fun closeTransport(clientId: Long,clientEmailSettingId: Long) = emailServiceProviderConnectionFactory.closeSMTPTransportConnection(clientId,clientEmailSettingId)


    fun insertUnsubscribeLinkIfApplicable(content: String, unsubscribeLink: String, clientId: Long, mongoEmailId: String): String {
        val unsubscribePlaceholder = "##UND_UNSUBSCRIBE_LINK##"
        val content = unsbsrbPlcHldrCntnt(unsubscribeLink, clientId, mongoEmailId)
        return content.replace(unsubscribePlaceholder, content)
    }

    fun getUnsubscribeLink(clientUnsubscribeLink: String, clientId: Long, mongoEmailId: String): String {
        return unsbsrbPlcHldrCntnt(clientUnsubscribeLink, clientId, mongoEmailId)
    }

    private fun unsbsrbPlcHldrCntnt(unsubscribeLink: String, clientId: Long, mongoEmailId: String): String {
        val qmark = if (unsubscribeLink.contains('?')) "&" else "?"
        return "$unsubscribeLink$qmark" + "c=$clientId&e=$mongoEmailId"
    }

    fun addPixelTracking(content: String, clientId: Long, mongoEmailId: String): String {
        var doc = Jsoup.parse(content)
        val imageUrl = getImageUrl(clientId, mongoEmailId)
        doc.body().append("""<div><img src="$imageUrl"></div>""")
        return doc.body().html().toString()
    }

    fun getImageUrl(clientId: Long, mongoEmailId: String): String {
        val id = clientId.toString() + "&" + mongoEmailId
        return getImageUrl(id)
    }

    private fun getImageUrl(id: String): String {
        return "$eventApiUrl/email/image/${id}.jpg"
    }

    fun trackAllURLs(content: String, clientId: Long, mongoEmailId: String): String {
        val urlRegex = "((https?):(//)+[\\w\\d:#@%/;\$()~_?+-=\\\\.&]*)".toRegex(RegexOption.IGNORE_CASE)
        //FIXME hardcoded urls

        val trackingURL = "https://userndot.com/event/track"
        val excludeTrackingURLs = arrayOf(
                "^(https?)://(www.)?userndot.com.*\$"
        )

        return urlRegex.findAll(content).map { urlMatch ->
            urlMatch.value
        }.filter { url ->
            excludeTrackingURLs.map { exclude -> !url.matches(exclude.toRegex()) }.reduce { a, b -> a || b }
        }.fold(content) { value, url ->
            //val url = it.value
            value.replace(url,
                    "$trackingURL?c=$clientId&e=$mongoEmailId&u=" + URLEncoder.encode(url, "UTF-8"))
        }
    }
    //TODO jedis connection error.
//    @Cacheable(key = "'client_'+#clientId+'setting_id_'+#clientEmailSettingId",cacheNames = ["serviceProviderCredentials"])
    fun getEmailServiceProviderCredentials(clientId: Long, clientEmailSettingId: Long): ServiceProviderCredentials {
        logger.info("Getting service provider for client ${clientId} and clientEmailSetting ${clientEmailSettingId}")
        val clientEmailSetting = clientEmailSettingsRepository.findById(clientEmailSettingId)
        if (!clientEmailSetting.isPresent) throw Exception("Client Email Setting not exists for id ${clientEmailSettingId} client ${clientId}")
        else {
            val sp = serviceProviderCredentialsRepository.findByClientIDAndId(clientId,clientEmailSetting.get().serviceProviderId!! )
            if (sp.isPresent) {
                return sp.get()
            } else {
                return ServiceProviderCredentials()
            }
        }
    }

}