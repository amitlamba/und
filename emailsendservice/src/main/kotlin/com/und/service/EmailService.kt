package com.und.service

import com.netflix.discovery.converters.Auto
import com.und.common.utils.EmailServiceUtility
import com.und.common.utils.ReplaceNullPropertyOfEventUser
import com.und.config.EventStream
import com.und.exception.EmailError
import com.und.exception.EmailFailureException
import com.und.model.mongo.EmailStatus.NOT_SENT
import com.und.model.mongo.EmailStatus.SENT
import com.und.model.mongo.EventUser
import com.und.model.utils.*
import com.und.model.utils.eventapi.Event
import com.und.model.utils.eventapi.Identity
import com.und.repository.jpa.ClientSettingsRepository
import com.und.repository.jpa.EmailTemplateRepository
import com.und.repository.jpa.security.UserRepository
import com.und.utils.loggerFor
import org.apache.commons.lang.StringUtils
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import java.util.regex.Pattern
import org.springframework.cache.annotation.Cacheable
import com.amazonaws.services.simpleemail.model.Message as SESMessage


@Service("emailservice")
class EmailService:CommonEmailService {
    companion object {
        protected val logger = loggerFor(EmailService::class.java)
    }


    @Autowired
    private lateinit var serviceProviderCredentialsService: ServiceProviderCredentialsService

    @Autowired
    private lateinit var clientSettingsRepository: ClientSettingsRepository

    @Autowired
    private lateinit var emailHelperService: EmailHelperService

    @Autowired
    private lateinit var emailSendService: EmailSendService

    @Autowired
    private lateinit var emailTemplateRepository: EmailTemplateRepository

    @Autowired
    private lateinit var eventStream: EventStream

    @Autowired
    private lateinit var eventApiFeignClient:EventApiFeignClient

    @Autowired
    private lateinit var userRepository:UserRepository

//    private var wspCredsMap: MutableMap<Long, ServiceProviderCredentials> = mutableMapOf()

    @Autowired
    private lateinit var emailServiceUtility:EmailServiceUtility


//    fun sendEmailBySMTP(emailSMTPConfig: EmailSMTPConfig, email: Email) {
//        emailSendService.sendEmailBySMTP(emailSMTPConfig, email)
//    }
//
//    fun sendEmailByAWSSDK(emailSESConfig: EmailSESConfig, email: Email) {
//        emailSendService.sendEmailByAWSSDK(emailSESConfig, email)
//    }

    @Value("\${und.url.event}")
    private lateinit var unsubscribeLink:String

    override fun sendEmail(email: Email) {

        val variable=getVariableFromTemplate(email)

        val user:EventUser? = ReplaceNullPropertyOfEventUser.replaceNullPropertyOfEventUser(email.eventUser, variable)

        fun String.addUrlTracking(uniqueTrackingId: String): String {
            return emailHelperService.trackAllURLs(this, email.clientID, uniqueTrackingId)
        }

        fun String.addPixelTracking(uniqueTrackingId: String): String {
            return emailHelperService.addPixelTracking(this, email.clientID, uniqueTrackingId)
        }

        val emailToSend = email.copy()
        val model = emailToSend.data
        //FIXME: cache the findByClientID clientSettings
        val clientSettings = clientSettingsRepository.findByClientID(emailToSend.clientID)
        val mongoEmailId = ObjectId().toString()
        emailToSend.mongoNotificationId=mongoEmailId
        user?.let {
            model["user"]=it
        }
        if (StringUtils.isNotBlank(clientSettings?.unSubscribeLink))
            model["unsubscribeLink"] = emailHelperService.getUnsubscribeLink(clientSettings?.unSubscribeLink!!, emailToSend.clientID, mongoEmailId)
        else model["unsubscribeLink"]="$unsubscribeLink/email/unsubscribe"
        model["pixelTrackingPlaceholder"] = """<div><img src="""" + emailHelperService.getImageUrl(emailToSend.clientID, mongoEmailId) + """">"""

        val (subject, body) = emailHelperService.subjectAndBody(emailToSend)


        emailToSend.emailBody = body.addUrlTracking(mongoEmailId)
//                .addPixelTracking(mongoEmailId)
        emailToSend.emailSubject = subject

//        if (isSystemClient(email)) {
//            val template = emailTemplateRepository.findByIdAndClientID(email.emailTemplateId, email.clientID)
//            val from = template.map { it.from }
//            if (from.isPresent) {
//                emailToSend.fromEmailAddress = InternetAddress(from.get())
//            } else {
//
//                logger.error("from email for template id ${email.emailTemplateId} is not present for system user")
//                val error = EmailError()
//                with(error) {
//                    clientid = email.clientID
//                    failureType = EmailError.FailureType.CONNECTION
//                    causeMessage = "from email for template id ${email.emailTemplateId} is not present for system user"
//                    failedSettingId = clientSettings?.id
//
//                }
//                throw  EmailFailureException("from email for template id ${email.emailTemplateId} is not present for system user", error)
//            }
//        }
        emailHelperService.saveMailInMongo(emailToSend, NOT_SENT, mongoEmailId)
        emailServiceUtility.sendEmailWithoutTracking(emailToSend)
        emailHelperService.updateEmailStatus(mongoEmailId, SENT, emailToSend.clientID)

        //TODO this event is track only for campaign not for system emails
        val token = userRepository.findSystemUser().key
        var event= Event()
        with(event) {
            name = "Notification Sent"
            clientId=emailToSend.clientID
            notificationId=mongoEmailId
            attributes.put("campaign_id",emailToSend.campaignId)
            attributes.put("template_id",emailToSend.emailTemplateId)
            userIdentified=true
            identity= Identity(userId = email.eventUser?.id,clientId = emailToSend.clientID.toInt())

        }
        eventApiFeignClient.pushEvent(token,event)
    }


    @Cacheable(value = ["templateVariable"],key = "'email_template_variable'+#email.clientID+'_'+#email.emailTemplateId" )
    fun getVariableFromTemplate(email: Email):Set<String>{
        val listOfVariable = mutableSetOf<String>()

        val template=emailTemplateRepository.findByIdAndClientID(email.emailTemplateId,email.clientID)
        if(template.isPresent){
            val tem=template.get()
            val subject=tem.emailTemplateSubject?.template ?: ""
            val body=tem.emailTemplateBody?.template ?: ""
            val regex="(\\$\\{.*?\\})"
            val pattern = Pattern.compile(regex)

            val subjectMatcher = pattern.matcher(subject)
            val bodyMatcher = pattern.matcher(body)
            var i=0
            while (subjectMatcher.find()){
                listOfVariable.add(subjectMatcher.group(i+1))
            }
            i=0
            while (bodyMatcher.find()){
                listOfVariable.add(bodyMatcher.group(i+1))
            }

        }

        return listOfVariable
    }

//    private fun isSystemClient(email: Email) = email.clientID == 1L

//    fun sendEmailWithoutTracking(email: Email) {
//        val serviceProviderCredential = serviceProviderCredentials(email = email)
////        val serviceProviderCredential= emailHelperService.getEmailServiceProviderCredentials(email.clientID,email.clientEmailSettingId!!)
////        val spcrd=serviceProviderCredentialsService.buildWebServiceProviderCredentials(serviceProviderCredential)
//        sendEmail(serviceProviderCredential, email)
//    }

//    private fun sendEmail(serviceProviderCredential: ServiceProviderCredentials, email: Email) {
//        when (serviceProviderCredential.serviceProvider) {
//            ServiceProviderCredentialsService.ServiceProvider.SMTP.desc,
//            ServiceProviderCredentialsService.ServiceProvider.AWS_SES_SMTP.desc -> {
//                val emailSMTPConfig = EmailSMTPConfig.build(serviceProviderCredential,email.clientEmailSettingId)
//                sendEmailBySMTP(emailSMTPConfig, email)
//            }
//            ServiceProviderCredentialsService.ServiceProvider.AWS_SES_API.desc -> {
//                val emailSESConfig = EmailSESConfig.build(serviceProviderCredential,email.clientEmailSettingId)
//                sendEmailByAWSSDK(emailSESConfig, email)
//            }
//
//        }
//    }

//    private fun serviceProviderCredentials(email: Email): ServiceProviderCredentials {
//        synchronized(email.clientID) {
//            //TODO: This code can be cached in Redis
//            if (!wspCredsMap.containsKey(email.clientID)) {
//                val webServiceProviderCred = serviceProviderCredentialsService.getServiceProviderCredentials(email)
//                wspCredsMap[email.clientID] = webServiceProviderCred
//            }
//        }
//        return wspCredsMap[email.clientID]!!
//    }

    fun sendVerificationEmail(email: Email) {
        //update subject and body using template
        var templateId = email.emailTemplateId
        var templateName = email.emailTemplateName

        //var emailToSend=emailHelperService.updateSubjectAndBody(email)
        emailServiceUtility.sendEmailWithoutTracking(email)
    }

    fun toKafkaEmailError(emailError: EmailError): Boolean =
            eventStream.emailFailureEventSend().send(MessageBuilder.withPayload(emailError).build())
}
