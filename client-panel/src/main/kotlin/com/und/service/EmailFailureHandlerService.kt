package com.und.service

import com.und.common.utils.loggerFor
import com.und.exception.EmailError
import com.und.model.Status
import com.und.model.jpa.ClientSettingsEmail
import com.und.model.jpa.EmailFailureAuditLog
import com.und.model.mongo.BlockHistory
import com.und.model.mongo.BlockedEmail
import com.und.repository.jpa.ClientSettingsEmailRepository
import com.und.repository.jpa.EmailFailureAuditLogRepository
import com.und.repository.mongo.BlockedEmailRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.Cacheable
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.data.domain.Example
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.und.web.model.Campaign as WebCampaign


@Service
class EmailFailureHandlerService {


    companion object {

        protected val logger = loggerFor(EmailFailureHandlerService::class.java)
    }


    @Autowired
    private lateinit var userSettingsService: UserSettingsService

    @Autowired
    private lateinit var emailService: EmailService

    @Autowired
    private lateinit var campaignService: CampaignService

    @Autowired
    private lateinit var emailFailureAuditLogRepository: EmailFailureAuditLogRepository

    @Autowired
    private lateinit var blockedEmailRepository: BlockedEmailRepository

    @Autowired
    private lateinit var clientSettingsEmailRepository: ClientSettingsEmailRepository


    @StreamListener("emailFailureEventReceive")
    @Transactional
    fun emailFailureHandler(emailError: EmailError) {
        val failureType = emailError.failureType


        when (failureType) {
            EmailError.FailureType.CONNECTION -> handleEmailConnectionFailure(emailError)

            EmailError.FailureType.DELIVERY -> handleEmailDeliveryFailure(emailError)
            EmailError.FailureType.INCORRECT_EMAIL -> handleEmailDeliveryFailure(emailError)
            EmailError.FailureType.OTHER -> handleOtherEmailFailure(emailError)
            EmailError.FailureType.NONE -> handleOtherEmailFailure(emailError)
        }


    }

    private fun handleEmailConnectionFailure(emailError: EmailError) {
        //log error
        saveEmailFailureLog(emailError)

        emailError.clientid?.let { clientId ->
            //mark status as inactive
            emailError.failedSettingId?.let {
                val setting=getClientEmailSetting(clientId,it)
                setting?.let {
                    val providers = userSettingsService.getEmailServiceProvider(clientId,it.serviceProviderId!!)
                    if (providers!=null) {
                        userSettingsService.saveEmailServiceProvider(providers, Status.DISABLED)
                    }
                }
            }

            //pause forced campaign
//            campaignService.pauseAllRunning(clientId)
            //send an email
            emailService.sendEmailConnectionErrorEmail(emailError)
        }


    }

    @Cacheable(key = "'client_'+#clientId+'id_'+#id",cacheNames = ["clientemailsettings"])
    private fun getClientEmailSetting(clientId:Long,id:Long):ClientSettingsEmail?{
        val setting=clientSettingsEmailRepository.findByClientIdAndId(clientId,id)
        if(setting.isPresent) return setting.get()
        else {logger.error("Client Email Setting not present for client $clientId id $id"); return null}
    }
    private fun handleEmailDeliveryFailure(emailError: EmailError) {
        //log error
        saveEmailFailureLog(emailError)
        emailError.clientid?.let { clientId ->
            val invalidAddresses: List<String> = emailError.invalidAddresses
            // val validSentAddresses: List<String> = emailError
            val validUnsentAddresses: List<String> = emailError.unsentAddresses
            //log failed email ids
            val failedEmails = mutableListOf<BlockHistory>()
            invalidAddresses.forEach { email ->
                failedEmails.add(BlockHistory(email = email, message = emailError.causeMessage ?: ""))
            }
            validUnsentAddresses.forEach { email ->
                failedEmails.add(BlockHistory(email = email, message = emailError.causeMessage ?: ""))
            }
            val blockedEmail = BlockedEmail(clientId = clientId, history = failedEmails)
            saveFailedEmail(clientId, blockedEmail)

            //send email at end of day
        }

    }

    private fun handleOtherEmailFailure(emailError: EmailError) {
        //log error
        saveEmailFailureLog(emailError)

    }

    private fun saveEmailFailureLog(emailError: EmailError) {
        val log = EmailFailureAuditLog()
        with(log) {
            log.clientID = emailError.clientid
            log.clientSettingId = emailError.failedSettingId
            log.message = emailError.causeMessage
            log.status = emailError.failureType

        }
        emailFailureAuditLogRepository.save(log)
    }


    private fun saveFailedEmail(clientId: Long, blockedEmail: BlockedEmail) {
        blockedEmailRepository.appendHistory(clientId, blockedEmail)
    }


    fun connectionErrors(clientId: Long): List<EmailFailureAuditLog> {
        val auditlog = EmailFailureAuditLog()
        auditlog.clientID = clientId
        val example: Example<EmailFailureAuditLog> = Example.of(auditlog)
        val allAuditLogs = emailFailureAuditLogRepository.findAll(example, Sort.by("dateCreated"))
        return allAuditLogs ?: emptyList()
    }


}