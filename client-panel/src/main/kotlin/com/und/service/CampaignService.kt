package com.und.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.common.utils.loggerFor
import com.und.config.EventStream
import com.und.web.controller.exception.UndBusinessValidationException
import com.und.model.CampaignStatus
import com.und.model.JobActionStatus
import com.und.model.JobDescriptor
import com.und.model.TriggerDescriptor
import com.und.model.jpa.*
import com.und.repository.jpa.CampaignAuditLogRepository
import com.und.repository.jpa.CampaignRepository
import com.und.repository.jpa.ClientSettingsRepository
import com.und.security.utils.AuthenticationUtils
import com.und.web.model.ValidationError
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.util.*
import com.und.web.model.Campaign as WebCampaign


@Service

class CampaignService {


    companion object {

        protected val logger = loggerFor(CampaignService::class.java)
    }

    @Autowired
    private lateinit var campaignRepository: CampaignRepository

    @Autowired
    private lateinit var userSettingsService: UserSettingsService

    @Autowired
    private lateinit var campaignAuditRepository: CampaignAuditLogRepository

    @Autowired
    private lateinit var eventStream: EventStream

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    fun getCampaigns(): List<WebCampaign> {
        val campaigns = AuthenticationUtils.clientID?.let {
            campaignRepository.findByClientID(it)
        }

        return campaigns?.map { buildWebCampaign(it) } ?: listOf()
    }


    fun save(webCampaign: WebCampaign): WebCampaign {
        val persistedCampaign = saveCampaign(webCampaign)
        return if (persistedCampaign != null) buildWebCampaign(persistedCampaign) else WebCampaign()
    }

    @Transactional
    protected fun saveCampaign(webCampaign: com.und.web.model.Campaign): Campaign? {
        val campaign = buildCampaign(webCampaign)

        val persistedCampaign = campaignRepository.save(campaign)

        webCampaign.id = persistedCampaign.id
        logger.info("sending request to scheduler ${campaign.name}")
        val jobDescriptor = buildJobDescriptor(webCampaign, JobDescriptor.Action.CREATE)
        sendToKafka(jobDescriptor)
        return persistedCampaign
    }

    private fun buildJobDescriptor(campaign: WebCampaign, action: JobDescriptor.Action): JobDescriptor {

        fun buildTriggerDescriptor(): TriggerDescriptor {
            val triggerDescriptor = TriggerDescriptor()
            with(triggerDescriptor) {
                val recurring = campaign.schedule?.recurring
                val oneTime = campaign.schedule?.oneTime
                val multipleDates = campaign.schedule?.multipleDates
                when {
                    recurring != null -> {
                        cron = recurring.cronExpression
                        endDate = recurring.scheduleEnd?.endsOn
                        startDate = recurring.scheduleStartDate
                        countTimes = recurring.scheduleEnd?.occurrences ?: 0

                    }
                    oneTime != null -> {

                        fireTime = oneTime.campaignDateTime?.toLocalDateTime()
                    }
                    multipleDates != null -> {
                        fireTimes = multipleDates.campaignDateTimeList.map { it.toLocalDateTime() }
                    }
                }
            }

            return triggerDescriptor
        }

        val jobDescriptor = JobDescriptor()
        jobDescriptor.timeZoneId = userSettingsService.getTimeZone()
        jobDescriptor.campaignName = campaign.name
        jobDescriptor.clientId = AuthenticationUtils.clientID.toString()
        jobDescriptor.campaignId = campaign.id.toString()
        jobDescriptor.action = action



        val triggerDescriptors = arrayListOf<TriggerDescriptor>()

        triggerDescriptors.add(buildTriggerDescriptor())
        jobDescriptor.triggerDescriptors = triggerDescriptors
        return jobDescriptor
    }





    fun buildCampaign(webCampaign: WebCampaign): Campaign {
        val campaign = Campaign()
        with(campaign) {
            this.id = webCampaign.id
            this.clientID = AuthenticationUtils.clientID
            name = webCampaign.name
            appuserID = AuthenticationUtils.principal.id
            campaignType = webCampaign.campaignType
            segmentationID = webCampaign.segmentationID


            schedule = objectMapper.writeValueAsString(webCampaign.schedule)
        }

        when (webCampaign.campaignType) {
            CampaignType.EMAIL -> {
                val emailcampaign = EmailCampaign()
                emailcampaign.appuserId = campaign.appuserID
                emailcampaign.clientID = campaign.clientID
                emailcampaign.templateId = webCampaign.templateID
                campaign.emailCampaign = emailcampaign
            }
            CampaignType.SMS -> {
                val smscampaign = SmsCampaign()
                smscampaign.appuserId = campaign.appuserID
                smscampaign.clientID = campaign.clientID
                smscampaign.templateId = webCampaign.templateID
                campaign.smsCampaign = smscampaign
            }
            else -> {
            }
        }

        return campaign
    }


    fun buildWebCampaign(campaign: Campaign): WebCampaign {
        val webCampaign = WebCampaign()
        with(webCampaign) {
            id = campaign.id
            name = campaign.name

            campaignType = campaign.campaignType
            segmentationID = campaign.segmentationID
            dateCreated = campaign.dateCreated
            dateModified = campaign.dateModified
            status = campaign.status



            schedule = objectMapper.readValue(campaign.schedule, Schedule::class.java)
        }

        if (campaign.emailCampaign != null) {
            val emailcampaign = campaign.emailCampaign
            webCampaign.templateID = emailcampaign?.templateId
            webCampaign.campaignType = CampaignType.EMAIL
        } else if (campaign.smsCampaign != null) {
            val smsCampaign = campaign.smsCampaign
            webCampaign.templateID = smsCampaign?.templateId
            webCampaign.campaignType = CampaignType.SMS
        }
        return webCampaign
    }
    fun buildWebCampaignForList(campaign: Campaign): WebCampaign {
        val webCampaign = WebCampaign()
        with(webCampaign) {
            id = campaign.id
            name = campaign.name

            campaignType = campaign.campaignType
            segmentationID = campaign.segmentationID
            dateCreated = campaign.dateCreated
            dateModified = campaign.dateModified
            status = campaign.status



            schedule = objectMapper.readValue(campaign.schedule, Schedule::class.java)
        }

/*        if (campaign.emailCampaign != null) {
            val emailcampaign = campaign.emailCampaign
            webCampaign.templateID = emailcampaign?.templateId
            webCampaign.campaignType = CampaignType.EMAIL
        } else if (campaign.smsCampaign != null) {
            val smsCampaign = campaign.smsCampaign
            webCampaign.templateID = smsCampaign?.templateId
            webCampaign.campaignType = CampaignType.SMS
        }*/
        return webCampaign
    }

    fun pause(campaignId: Long): Long? {
        return handleSchedule(campaignId, JobDescriptor.Action.PAUSE)
    }

    fun resume(campaignId: Long): Long? {
        return handleSchedule(campaignId, JobDescriptor.Action.RESUME)
    }

    fun stop(campaignId: Long): Long? {
        return handleSchedule(campaignId, JobDescriptor.Action.STOP)
    }

    fun delete(campaignId: Long): Long? {
        return handleSchedule(campaignId, JobDescriptor.Action.DELETE)
    }


    private fun handleSchedule(campaignId: Long, action: JobDescriptor.Action): Long {
        val campaignOption = campaignRepository.findById(campaignId)

        val campaign = if (campaignOption.isPresent) campaignOption.get() else {
            val error = ValidationError()
            error.addFieldError("campaignId", "No Campaign With id $campaignId exists")
            throw UndBusinessValidationException(error)
        }

        if (campaign.status == actionToCampaignStatus(action)) {
            val error = ValidationError()
            error.addFieldError("campaignId", "Campaign  already has status of  ${campaign.status}")
            throw UndBusinessValidationException(error)
        }


        when {
            campaign.status == CampaignStatus.DELETED -> {
                val error = ValidationError()
                error.addFieldError("campaignId", "Campaign is deleted and  ${action.name} cant be performed")
                throw UndBusinessValidationException(error)
            }
            campaign.status == CampaignStatus.STOPPED && action != JobDescriptor.Action.DELETE -> {
                val error = ValidationError()
                error.addFieldError("campaignId", "Campaign is stopped and  ${action.name}  cant be performed")
                throw UndBusinessValidationException(error)
            }
            else -> {
            }
        }

        val jobDescriptor = JobDescriptor()
        jobDescriptor.clientId = AuthenticationUtils.clientID.toString()
        jobDescriptor.campaignId = campaignId.toString()
        jobDescriptor.action = action
        jobDescriptor.campaignName = campaign.name//set because it cant be null FIXME find some other way around

        sendToKafka(jobDescriptor)
        return campaignId
    }


    fun sendToKafka(jobDescriptor: JobDescriptor) = eventStream.scheduleJobSend().send(MessageBuilder.withPayload(jobDescriptor).build())


    @StreamListener("scheduleJobAckReceive")
    @Transactional
    fun schedulerAcknowledge(jobActionStatus: JobActionStatus) {
        val status = jobActionStatus.status
        val action = jobActionStatus.jobAction
        val clientId = action.clientId.toLong()
        val campaignId = action.campaignId.toLong()
        val campignName = action.campaignName
        val actionPerformed = action.action
        when {
            status == JobActionStatus.Status.OK -> {
                campaignRepository.updateScheduleStatus(campaignId, clientId, actionToCampaignStatus(actionPerformed).name)
                logger.info(" Campaign Schedule created $campaignId")
            }
            actionPerformed == JobDescriptor.Action.CREATE -> {
                campaignRepository.updateScheduleStatus(campaignId, clientId, CampaignStatus.ERROR.name)
                logger.error(" Campaign Schedule couldn't be created for $campaignId")
            }
            else -> {
                logger.error("  Schedule action couldn't be performed for $campaignId")
            }
        }

        val auditLog = CampaignAuditLog()
        auditLog.campaignId = campaignId
        auditLog.clientID = clientId
        auditLog.status = status
        auditLog.action = action.action
        auditLog.message = jobActionStatus.message

        campaignAuditRepository.save(auditLog)

    }

    fun actionToCampaignStatus(action: JobDescriptor.Action): CampaignStatus {
        return when (action) {
            JobDescriptor.Action.PAUSE -> CampaignStatus.PAUSED
            JobDescriptor.Action.RESUME -> CampaignStatus.RESUMED
            JobDescriptor.Action.DELETE -> CampaignStatus.DELETED
            JobDescriptor.Action.CREATE -> CampaignStatus.CREATED
            JobDescriptor.Action.STOP -> CampaignStatus.STOPPED
            JobDescriptor.Action.NOTHING -> CampaignStatus.ERROR
        }
    }

}