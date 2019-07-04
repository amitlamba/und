package com.und.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.common.utils.loggerFor
import com.und.config.EventStream
import com.und.model.*
import com.und.model.TestCampaign
import com.und.model.jpa.*
import com.und.model.jpa.Campaign
import com.und.model.redis.LiveSegmentCampaign
import com.und.model.redis.LiveSegmentCampaignCache
import com.und.repository.jpa.CampaignAuditLogRepository
import com.und.repository.jpa.CampaignRepository
import com.und.repository.jpa.ClientSettingsEmailRepository
import com.und.repository.jpa.CustomFromAddrAndSrpRepository
import com.und.repository.redis.LiveSegmentCampaignRepository
import com.und.security.utils.AuthenticationUtils
import com.und.web.controller.exception.CustomException
import com.und.web.controller.exception.ScheduleUpdateException
import com.und.web.controller.exception.UndBusinessValidationException
import com.und.web.model.*
import com.und.web.model.AbCampaign
import com.und.web.model.Variant
import org.hibernate.exception.ConstraintViolationException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.messaging.support.MessageBuilder
import org.springframework.scheduling.support.CronSequenceGenerator
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.*
import java.util.*
import kotlin.Comparator
import com.und.web.model.Campaign as WebCampaign
import com.und.web.model.TestCampaign as WebTestCampaign
import com.und.model.jpa.AbCampaign as JpaAbCampaign
import com.und.model.jpa.Variant as JpaVariant

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
    private lateinit var clientSettingsEmailRepository: ClientSettingsEmailRepository

    @Autowired
    private lateinit var eventStream: EventStream

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var androidService: AndroidService

    @Autowired
    private lateinit var webPushService: WebPushService

    @Autowired
    private lateinit var clientFromAddrAndSrpRepository: CustomFromAddrAndSrpRepository

    @Autowired
    private lateinit var liveSegmentCampaignRepository: LiveSegmentCampaignRepository

    fun getCampaigns(): List<WebCampaign> {
        val campaigns = AuthenticationUtils.clientID?.let {
            campaignRepository.findByClientID(it)
        }

        return campaigns?.map { buildWebCampaign(it) } ?: listOf()
    }

    fun getCampaignById(campaignId: Long): WebCampaign {
        var clientId = AuthenticationUtils.clientID ?: throw AccessDeniedException("")
        var campaign = campaignRepository.findByIdAndClientID(campaignId, clientId)
        if (campaign.isPresent) {
            return buildWebCampaign(campaign = campaign.get())
        } else {
            throw ScheduleUpdateException("Campaign doesn't exist with id $campaignId and client : $clientId")
        }
    }

    fun save(webCampaign: WebCampaign): WebCampaign {
        val persistedCampaign = saveCampaign(webCampaign)
        return if (persistedCampaign != null) buildWebCampaign(persistedCampaign) else WebCampaign()
    }

    fun saveAbCampaign(abCampaign: AbCampaign, clientId: Long) {
        //val jpaAbCampaign = buildJpaAbCampaign(abCampaign)
        //val jpaVariant = buildJpaVariant(abCampaign.variants)
        val campaign = abCampaign.campaign
        campaign?.abCampaign = abCampaign
        campaign?.variants = abCampaign.variants
        campaign?.let { saveCampaign(it) }
    }

    private fun buildJpaAbCampaign(abCampaign: AbCampaign): JpaAbCampaign {
        val campaign = JpaAbCampaign()
        with(campaign) {
            sampleSize = abCampaign.sampleSize
            runType = abCampaign.runType
            remind = abCampaign.remind
            waitTime = abCampaign.waitTime
        }
        return campaign
    }

    private fun buildJpaVariant(variants: List<Variant>): List<JpaVariant> {
        val jpaVariants = mutableListOf<JpaVariant>()
        variants.forEach {
            var variant = JpaVariant()
            with(variant) {
                percentage = it.percentage
                templateId = it.templateId
                name = it.name
                users = it.users
            }
            jpaVariants.add(variant)
        }
        return jpaVariants
    }

    @Transactional
    protected fun saveCampaign(webCampaign: com.und.web.model.Campaign): Campaign? {

        val campaign = buildCampaign(webCampaign)
        //campaign.abCampaign?.campaign = campaign
        try {
            val persistedCampaign = campaignRepository.save(campaign)

            webCampaign.id = persistedCampaign.id
            if (persistedCampaign != null) {
                campaignRepository.updateScheduleStatus(persistedCampaign.id!!, persistedCampaign.clientID!!, CampaignStatus.CREATED.name)
            }
            if (webCampaign.schedule != null) {
                //In case of live campaign its not scheduled.
                logger.info("sending request to scheduler ${campaign.name}")
                scheduleJob(webCampaign)
            } else {
                val campaigns = getLiveSegmentCampaignsR(persistedCampaign.clientID!!,persistedCampaign.segmentationID!!).toMutableList()
                val liveSegmentCampaign = LiveSegmentCampaign()
                with(liveSegmentCampaign) {
                    campaignId = persistedCampaign.id!!
                    status = "CREATED"
                    startDate = persistedCampaign.startDate
                }
                campaigns.add(liveSegmentCampaign)
                updateLiveSegmentCampaignsR(persistedCampaign.clientID!!, persistedCampaign.segmentationID!!, campaigns)

            }
            return persistedCampaign
        } catch (ex: ConstraintViolationException) {
            logger.error("Exception In Campaign Save clientId ${campaign.clientID} constaintsName ${ex.constraintName} cause ${ex.cause?.message}")
            throw CustomException("Campaign with this name already exists.${ex.message}")
        } catch (ex: DataIntegrityViolationException) {
            logger.error("Exception In Campaign Save clientId ${campaign.clientID} cause ${ex.message}")
            throw CustomException("Campaign with this name already exists.${ex.message} ${ex.cause?.message}")
        }
        return null
    }

    @CachePut(value = ["liveSegmentCampaigns"], key = "'clientId_'+#clientId+'segmentId_'+#segmentId")
    fun updateLiveSegmentCampaigns(clientId: Long, segmentId: Long, liveSegmentCampaign: List<LiveSegmentCampaign>):List<LiveSegmentCampaign> {
        return liveSegmentCampaign
    }

    @Cacheable(value = ["liveSegmentCampaigns"], key = "'clientId_'+#clientId+'segmentId_'+#segmentId")
    fun getLiveSegmentCampaigns(clientId: Long, segmentId: Long): List<LiveSegmentCampaign> {
        return emptyList()
    }

    //@CachePut(value = ["liveSegmentCampaigns"], key = "'clientId_'+#clientId+'segmentId_'+#segmentId")
    fun updateLiveSegmentCampaignsR(clientId: Long, segmentId: Long, liveSegmentCampaign: List<LiveSegmentCampaign>) {
        var liveSegmentCampaignCache = LiveSegmentCampaignCache()
        liveSegmentCampaignCache.id = "clientId_${clientId}:segmentId_${segmentId}"
        liveSegmentCampaignCache.liveSegmentCampaign = liveSegmentCampaign
        liveSegmentCampaignRepository.save(liveSegmentCampaignCache)
        //return liveSegmentCampaign
    }

    //@Cacheable(value = ["liveSegmentCampaigns"], key = "'clientId_'+#clientId+'segmentId_'+#segmentId")
    fun getLiveSegmentCampaignsR(clientId: Long, segmentId: Long): List<LiveSegmentCampaign> {
        val id = "clientId_${clientId}:segmentId_${segmentId}"
        val cacheResult=liveSegmentCampaignRepository.findById(id)
        return if (cacheResult.isPresent) cacheResult.get().liveSegmentCampaign else emptyList()
    }

    private fun scheduleJob(webCampaign: com.und.web.model.Campaign) {
        val jobDescriptor = buildJobDescriptor(webCampaign, JobDescriptor.Action.CREATE)
        val sendToKafka = sendToKafka(jobDescriptor)
    }

    @Transactional
    fun updateSchedule(campaignId: Long, clientId: Long, schedule: Schedule) {
        val cn = campaignRepository.findByIdAndClientID(campaignId, clientId)
                .orElseGet {
                    throw ScheduleUpdateException("Can't update schedule as campaign doesn't exist with id $campaignId and client : $clientId")
                }.apply {

                    if (status !in setOf(CampaignStatus.ERROR, CampaignStatus.SCHEDULE_PENDING, CampaignStatus.SCHEDULE_ERROR)) {
                        throw ScheduleUpdateException("Can't update schedule for campaign $campaignId and client : $clientId id \n as only those campaign with  schedule status as error can be updated")

                    }
                }

        cn.apply {
            val webCampaign = buildWebCampaign(this)
            webCampaign.schedule = schedule
            val jobDescriptor = buildJobDescriptor(webCampaign, JobDescriptor.Action.CREATE)
            logger.info("sending request to schedule, update schedule  for campaignId $campaignId and name $name for client $clientId")
            sendToKafka(jobDescriptor)
        }
        val scheduleJson = objectMapper.writeValueAsString(schedule)
        campaignRepository.updateSchedule(campaignId, clientId, scheduleJson)


    }

    private fun buildJobDescriptor(campaign: WebCampaign, action: JobDescriptor.Action): JobDescriptor {

        fun buildTriggerDescriptor(schedule: Schedule): TriggerDescriptor {
            val triggerDescriptor = TriggerDescriptor()
            with(triggerDescriptor) {
                val recurring = schedule.recurring
                val oneTime = schedule.oneTime
                val multipleDates = schedule.multipleDates
                when {
                    recurring != null -> {
                        cron = recurring.cronExpression
                        endDate = recurring.scheduleEnd?.endsOn
                        startDate = recurring.scheduleStartDate
                        countTimes = recurring.scheduleEnd?.occurrences ?: 0

                    }
                    oneTime != null -> {
                        fireTime = if (oneTime.nowOrLater == Now.Now) {
                            LocalDateTime.now().plusMinutes(1L)
                        } else {
                            oneTime.campaignDateTime?.toLocalDateTime()
                        }
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
        jobDescriptor.clientId = AuthenticationUtils.clientID.toString()
        jobDescriptor.action = action

        if (campaign.typeOfCampaign.equals(TypeOfCampaign.AB_TEST))
            jobDescriptor.jobDetail = buildJobDetail(campaign.id.toString(), campaign.name, jobDescriptor.clientId, true)
        else jobDescriptor.jobDetail = buildJobDetail(campaign.id.toString(), campaign.name, jobDescriptor.clientId)


        val triggerDescriptors = arrayListOf<TriggerDescriptor>()

        campaign.schedule?.let { triggerDescriptors.add(buildTriggerDescriptor(it)) }
        jobDescriptor.triggerDescriptors = triggerDescriptors
        return jobDescriptor
    }

    private fun buildJobDetail(campaignId: String, campaignName: String, clientId: String, abType: Boolean = false): JobDetail {
        val properties = CampaignJobDetailProperties()
        properties.campaignName = campaignName
        properties.campaignId = campaignId
        if (abType) properties.typeOfCampaign = TypeOfCampaign.AB_TEST.name


        val jobDetail = JobDetail()
        jobDetail.jobName = "${campaignId}-${campaignName}"
        jobDetail.jobGroupName = "${clientId}-${campaignId}"
        jobDetail.properties = properties
        return jobDetail
    }

    fun runTestCampaign(clientId: Long, testCampaign: WebTestCampaign) {

        val type = testCampaign.campaignType
        val testCampaign = when (type) {
            CampaignType.EMAIL -> buildTestEmailModel(clientId, testCampaign)
            CampaignType.SMS -> buildTestSmsModel(clientId, testCampaign)
            CampaignType.PUSH_ANDROID -> buildTestAndroidModel(clientId, testCampaign)
            CampaignType.PUSH_WEB -> buildTestWebModel(clientId, testCampaign)
            CampaignType.PUSH_IOS -> buildTestIosModel()
        }
        testCampaign(testCampaign, clientId)
    }

    fun buildTestEmailModel(clientId: Long, testWebCampaign: WebTestCampaign): TestCampaign {
        val testCampaign = TestCampaign()
        val emailTemplate = testWebCampaign.emailTemplate
        emailTemplate ?: throw CustomException("Template missing.")
        testWebCampaign.segmentationID
                ?: if (testWebCampaign.findByType == null || testWebCampaign.toAddresses == null) throw CustomException("")
        val serviceProviderId = testWebCampaign.serviceProviderId
        var fromUser = testWebCampaign.fromUser
        testWebCampaign.clientEmailSettingId?.let{
            val clientEmailSetting = clientSettingsEmailRepository.findByClientIdAndId(clientId,it)
            clientEmailSetting.ifPresent {
                fromUser = it.email
            }
        }
                ?: if (fromUser == null || serviceProviderId == null) throw CustomException("")
                else {
                    val clientSettingsEmail = clientSettingsEmailRepository.findByClientIdAndEmailAndServiceProviderId(clientId, fromUser?:"", serviceProviderId)
                    if (clientSettingsEmail.isPresent) testWebCampaign.clientEmailSettingId = clientSettingsEmail.get().id else throw CustomException("No client Email Setting found for email ${fromUser} and serviceProivder ${serviceProviderId}")
                }

        val campaign = WebCampaign()
        with(campaign) {
            segmentationID = testWebCampaign.segmentationID
            clientEmailSettingId = testWebCampaign.clientEmailSettingId
            campaignType = testWebCampaign.campaignType
            this.fromUser = fromUser
        }
        with(testCampaign) {
            this.clientId = clientId
            this.campaign = campaign
            type = CampaignType.EMAIL
            this.emailTemplate = emailTemplate
            this.findByType = testWebCampaign.findByType
            toAddresses = testWebCampaign.toAddresses?.split(",")?.toTypedArray()
        }
        return testCampaign
    }

    fun buildTestSmsModel(clientId: Long, testWebCampaign: WebTestCampaign): TestCampaign {
        val testCampaign = TestCampaign()
        val smsTemplate = testWebCampaign.smsTemplate
        smsTemplate ?: throw CustomException("Template missing.")
        testWebCampaign.segmentationID
                ?: if (testWebCampaign.findByType == null || testWebCampaign.toAddresses == null) throw CustomException("")

        val campaign = WebCampaign()
        with(campaign) {
            fromUser = testWebCampaign.fromUser
            segmentationID = testWebCampaign.segmentationID
            serviceProviderId = testWebCampaign.serviceProviderId
            campaignType = testWebCampaign.campaignType
        }
        with(testCampaign) {
            this.clientId = clientId
            this.campaign = campaign
            type = CampaignType.SMS
            this.smsTemplate = smsTemplate
            this.findByType = testWebCampaign.findByType
            toAddresses = testWebCampaign.toAddresses?.split(",")?.toTypedArray()
        }
        return testCampaign
    }

    fun buildTestAndroidModel(clientId: Long, testWebCampaign: WebTestCampaign): TestCampaign {
        val testCampaign = TestCampaign()
        val template = testWebCampaign.androidTemplate
        template ?: throw CustomException("Template missing.")
        val androidTemplate = androidService.buildJpaAndroidTemplate(template)
        testWebCampaign.segmentationID
                ?: if (testWebCampaign.findByType == null || testWebCampaign.toAddresses == null) throw CustomException("")
        val campaign = WebCampaign()
        with(campaign) {
            segmentationID = testWebCampaign.segmentationID
            serviceProviderId = testWebCampaign.serviceProviderId
            campaignType = testWebCampaign.campaignType
        }
        with(testCampaign) {
            this.clientId = clientId
            this.campaign = campaign
            type = CampaignType.PUSH_ANDROID
            this.androidTemplate = androidTemplate
            this.findByType = testWebCampaign.findByType
            toAddresses = testWebCampaign.toAddresses?.split(",")?.toTypedArray()
        }
        return testCampaign
    }

    fun buildTestWebModel(clientId: Long, testWebCampaign: WebTestCampaign): TestCampaign {
        val testCampaign = TestCampaign()
        val template = testWebCampaign.webPushTemplate
        template ?: throw CustomException("Template missing.")
        val webTemplate = webPushService.buildJpaWebPushTemplate(template)
        testWebCampaign.segmentationID
                ?: if (testWebCampaign.findByType == null || testWebCampaign.toAddresses == null) throw CustomException("")
        val campaign = WebCampaign()
        with(campaign) {
            segmentationID = testWebCampaign.segmentationID
            serviceProviderId = testWebCampaign.serviceProviderId
            campaignType = testWebCampaign.campaignType
        }
        with(testCampaign) {
            this.clientId = clientId
            this.campaign = campaign
            type = CampaignType.PUSH_WEB
            this.webTemplate = webTemplate
            this.findByType = testWebCampaign.findByType
            toAddresses = testWebCampaign.toAddresses?.split(",")?.toTypedArray()
        }
        return testCampaign
    }

    fun buildTestIosModel(): TestCampaign {
        return TestCampaign()
    }

    fun buildCampaign(webCampaign: WebCampaign): Campaign {
        val campaign = Campaign()

        with(campaign) {
            this.id = webCampaign.id
            this.clientID = AuthenticationUtils.clientID ?: throw AccessDeniedException("Access Denied.")
            name = webCampaign.name
            appuserID = AuthenticationUtils.principal.id
            campaignType = webCampaign.campaignType
            segmentationID = webCampaign.segmentationID
            serviceProviderId = webCampaign.serviceProviderId
            conversionEvent = webCampaign.conversionEvent
            fromUser = webCampaign.fromUser
            typeOfCampaign = webCampaign.typeOfCampaign
            webCampaign.abCampaign?.let { abCampaign = buildJpaAbCampaign(it) }
            webCampaign.variants?.let { variants = buildJpaVariant(it) }

            webCampaign.schedule?.oneTime?.let { whenTo ->
                if (whenTo.nowOrLater == Now.Now) {
                    whenTo.campaignDateTime = null
                }

            }
            if (webCampaign.schedule != null) schedule = objectMapper.writeValueAsString(webCampaign.schedule)
            else {
                var liveSchedule = webCampaign.liveSchedule

                liveSchedule?.let {
                    val startTime = when (it.nowOrLater) {
                        Now.Now -> LocalDateTime.now()
                        else -> it.startTime!!.toLocalDateTime()
                    }

                    var endTime: LocalDateTime? = null
                    it.endTime?.let {
                        endTime = it.toLocalDateTime()
                    }
                    if (endTime == null) endTime = LocalDateTime.now().plusYears(10)
                    campaign.startDate = startTime
                    campaign.endDate = endTime
                    //FIXME not saved ?
                    campaign.status = CampaignStatus.CREATED
                    campaign.schedule = "{}"
                }
            }
        }

        when (webCampaign.campaignType) {
            CampaignType.EMAIL -> {
//                if(webCampaign.fromUser.isNullOrEmpty()||webCampaign.serviceProviderId==null) throw CustomException("Email Campaign must have fromuser and serviceproviderid")
//                val clientSettingsEmail=clientSettingsEmailRepository.findByClientIdAndEmailAndServiceProviderId(campaign.clientID!!,webCampaign.fromUser!!,webCampaign.serviceProviderId!!)
                webCampaign.clientEmailSettingId?.let {
                    val clientSettingsEmail = clientSettingsEmailRepository.findByClientIdAndId(campaign.clientID!!, it)
                    if (!clientSettingsEmail.isPresent) throw CustomException("No client email setting found for client ${campaign.clientID} ,from email address ${webCampaign.fromUser} ,sp ${webCampaign.serviceProviderId}")
                    else {
                        val emailcampaign = EmailCampaign()
                        emailcampaign.appuserId = campaign.appuserID
                        emailcampaign.clientID = campaign.clientID
                        emailcampaign.templateId = webCampaign.templateID
                        emailcampaign.clientSettingEmailId = clientSettingsEmail.get().id
                        campaign.emailCampaign = emailcampaign
                        campaign.serviceProviderId = clientSettingsEmail.get().serviceProviderId
                    }
                }

            }
            CampaignType.SMS -> {
                val smscampaign = SmsCampaign()
                smscampaign.appuserId = campaign.appuserID
                smscampaign.clientID = campaign.clientID
                smscampaign.templateId = webCampaign.templateID
                campaign.smsCampaign = smscampaign
            }
            CampaignType.PUSH_ANDROID -> {
                var androidCampaign = AndroidCampaign()
                androidCampaign.appuserId = campaign.appuserID
                androidCampaign.clientId = campaign.clientID
                androidCampaign.templateId = webCampaign.templateID
                campaign.androidCampaign = androidCampaign
            }
            CampaignType.PUSH_WEB -> {
                var webPushCampaign = WebPushCampaign()
                webPushCampaign.appuserId = campaign.appuserID
                webPushCampaign.clientId = campaign.clientID
                webPushCampaign.templateId = webCampaign.templateID
                campaign.webCampaign = webPushCampaign
            }
//            CampaignType.PUSH_IOS ->{
//                var androidCampaign = AndroidCampaign()
//                androidCampaign.appuserId=campaign.appuserID
//                androidCampaign.clientId=campaign.clientID
//                androidCampaign.templateId=webCampaign.templateID
//                campaign.androidCampaign=androidCampaign
//            }
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
            conversionEvent = campaign.conversionEvent
            serviceProviderId = campaign.serviceProviderId
            fromUser = campaign.fromUser
            //Migrate db
            typeOfCampaign = campaign.typeOfCampaign

        }

        if (campaign.startDate != null) {
            val liveSchedule = LiveSchedule()
            liveSchedule.startTime = toCampaignTime(campaign.startDate)
            liveSchedule.endTime = toCampaignTime(campaign.endDate)
            webCampaign.liveSchedule = liveSchedule

        } else webCampaign.schedule = objectMapper.readValue(campaign.schedule, Schedule::class.java)

        if (campaign.emailCampaign != null) {
            val emailcampaign = campaign.emailCampaign
            webCampaign.templateID = emailcampaign?.templateId
            webCampaign.campaignType = CampaignType.EMAIL
        } else if (campaign.smsCampaign != null) {
            val smsCampaign = campaign.smsCampaign
            webCampaign.templateID = smsCampaign?.templateId
            webCampaign.campaignType = CampaignType.SMS
        } else if (campaign.androidCampaign != null) {
            val androidCampaign = campaign.androidCampaign
            webCampaign.campaignType = CampaignType.PUSH_ANDROID
            webCampaign.templateID = androidCampaign?.templateId
        } else if (campaign.webCampaign != null) {
            val webPushCampaign = campaign.webCampaign
            webCampaign.campaignType = CampaignType.PUSH_WEB
            webCampaign.templateID = webPushCampaign?.templateId
        }
//        else if(campaign.iosCampaign!=null){
//            val iosCampaign=campaign.iosCampaign
//            iosCampaign.campaignType=CampaignType.PUSH_IOS
//            iosCampaign.templateID=iosCampaign?.templateId
//        }

        //adding ab campaign
        campaign.abCampaign?.let {
            webCampaign.abCampaign = buildAbCampaign(it)
        }
        //adding variant
        campaign.variants?.let {
            webCampaign.variants = buildVariants(it)
        }


        return webCampaign
    }

    private fun buildAbCampaign(abCampaign: com.und.model.jpa.AbCampaign): AbCampaign {
        val abCampaign = AbCampaign()
        with(abCampaign) {
            id = abCampaign.id
            runType = abCampaign.runType
            remind = abCampaign.remind
            waitTime = abCampaign.waitTime
            sampleSize = abCampaign.sampleSize
        }
        return abCampaign
    }

    private fun buildVariants(variants: List<JpaVariant>): List<Variant> {
        val webVariants = mutableListOf<Variant>()
        variants.forEach {
            val variant = Variant()

            with(variant) {
                id = it.id
                percentage = it.percentage
                users = it.users
                name = it.name
                winner = it.winner
                templateId = it.templateId

            }
            webVariants.add(variant)
        }
        return webVariants
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

    @Transactional
    fun pauseAllRunning(clientId: Long): List<Long> {
        val runningCampaigns =
                campaignRepository.findByStatusIn(clientId, listOf(CampaignStatus.RESUMED, CampaignStatus.CREATED))
        runningCampaigns.forEach { campaignId ->
            handleSchedule(campaignId, JobDescriptor.Action.FORCE_PAUSE)
        }
        return emptyList()
    }


    @Transactional
    fun resumeAllForcePaused(clientId: Long): List<Long> {
        val forcePausedCampaigns =
                campaignRepository.findByStatusIn(clientId, listOf(CampaignStatus.FORCE_PAUSED))
        forcePausedCampaigns.forEach { campaignId ->
            resume(campaignId)
        }

        return emptyList()
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

    fun pauseLiveCampaign(clientId: Long, campaignId: Long) {
        val campaign = campaignRepository.findById(campaignId)
        campaign.ifPresent {
            if (it.startDate != null) {
                val status = it.status.toString()
                val order = LiveCampaignStatus.valueOf(status)
                if (order.ordinal < LiveCampaignStatus.PAUSED.ordinal) {
                    var liveCampaigns=getLiveSegmentCampaignsR(clientId,it.segmentationID!!)
                    liveCampaigns=liveCampaigns.map {
                        if(it.campaignId == campaignId) it.status = "PAUSE"
                        it
                    }
                    updateLiveSegmentCampaignsR(clientId,it.segmentationID!!,liveCampaigns)
                    campaignRepository.updateScheduleStatus(campaignId, clientId, CampaignStatus.PAUSED.name)
                }
            }
        }
    }

    fun deleteLiveCampaign(clientId: Long, campaignId: Long) {
        val campaign = campaignRepository.findById(campaignId)
        campaign.ifPresent {
            if (it.startDate != null) {
                val status = it.status.toString()
                val order = LiveCampaignStatus.valueOf(status)
                if (order.ordinal >= LiveCampaignStatus.STOPPED.ordinal) {
                    var liveCampaigns=getLiveSegmentCampaignsR(clientId,it.segmentationID!!)
                    liveCampaigns=liveCampaigns.filter {
                        it.campaignId!=campaignId
                    }
                    updateLiveSegmentCampaignsR(clientId,it.segmentationID!!,liveCampaigns)
                    campaignRepository.updateScheduleStatus(campaignId, clientId, CampaignStatus.DELETED.name)
                }
            }
        }
    }

    fun resumeLiveCampaign(clientId: Long, campaignId: Long) {
        val campaign = campaignRepository.findById(campaignId)
        campaign.ifPresent {
            if (it.startDate != null) {
                val status = it.status.toString()
                val order = LiveCampaignStatus.valueOf(status)
                if (order.ordinal < LiveCampaignStatus.STOPPED.ordinal) {
                    var liveCampaigns=getLiveSegmentCampaignsR(clientId,it.segmentationID!!)
                    liveCampaigns=liveCampaigns.map {
                        if(it.campaignId == campaignId) it.status = "CREATED"
                        it
                    }
                    updateLiveSegmentCampaignsR(clientId,it.segmentationID!!,liveCampaigns)
                    campaignRepository.updateScheduleStatus(campaignId, clientId, CampaignStatus.CREATED.name)
                }
            }
        }
    }

    fun stopLiveCampaign(clientId: Long, campaignId: Long) {
        val campaign = campaignRepository.findById(campaignId)
        campaign.ifPresent {
            if (it.startDate != null) {
                val status = it.status.toString()
                val order = LiveCampaignStatus.valueOf(status)
                if (order.ordinal < LiveCampaignStatus.COMPLETED.ordinal) {
                    var liveCampaigns=getLiveSegmentCampaignsR(clientId,it.segmentationID!!)
                    liveCampaigns=liveCampaigns.filter {
                        it.campaignId!=campaignId
                    }
                    updateLiveSegmentCampaignsR(clientId,it.segmentationID!!,liveCampaigns)
                    campaignRepository.updateScheduleStatus(campaignId, clientId, CampaignStatus.STOPPED.name)
                }
            }
        }
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
        jobDescriptor.clientId = campaign.clientID.toString()
        jobDescriptor.action = action
        //jobDescriptor.campaignName = campaign.name //set because it cant be null moved to below now FIXME find some other way around
        jobDescriptor.jobDetail = buildJobDetail(campaignId.toString(), campaign.name, jobDescriptor.clientId)

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
        val nextExecutionDate = action.nextTimeStamp
        when {
            status == JobActionStatus.Status.COMPLETED -> {

                val campaign = campaignRepository.findByIdAndClientID(campaignId, clientId)
                if (campaign.isPresent) {
                    when {
                        campaign.get().typeOfCampaign.equals(TypeOfCampaign.AB_TEST) && campaign.get().status.equals(CampaignStatus.AB_COMPLETED) -> {
                            if (nextExecutionDate == null) {
                                campaignRepository.updateScheduleStatus(campaignId, clientId, CampaignStatus.COMPLETED.name)
                                logger.info(" Campaign Schedule completed $campaignId")
                                auditCampaignLog(campaignId, clientId, status, action, jobActionStatus)
                                return
                            }
                        }
                        campaign.get().typeOfCampaign.equals(TypeOfCampaign.AB_TEST) && !campaign.get().status.equals(CampaignStatus.AB_COMPLETED) -> {
                            campaignRepository.updateScheduleStatus(campaignId, clientId, CampaignStatus.AB_COMPLETED.name)
                            //we are changing the jobActionStatus to ab completed because from scheduler we are returning completed .
                            val newActionStatus = createANewJobActionStatus(jobActionStatus)
                            auditCampaignLog(campaignId, clientId, newActionStatus.status, newActionStatus.jobAction, newActionStatus)
                            logger.info(" Campaign Schedule Ab completed $campaignId")
                            val runType = campaign.get().abCampaign?.runType ?: RunType.AUTO
                            if (nextExecutionDate == null) {
                                if (runType.equals(RunType.AUTO)) {
                                    campaignRepository.updateScheduleStatus(campaignId, clientId, CampaignStatus.COMPLETED.name)
                                    auditCampaignLog(campaignId, clientId, status, action, jobActionStatus)
                                    logger.info(" Campaign Schedule completed $campaignId")
                                } else {
                                    //Pausing the campaign because its manual type.
                                    campaignRepository.updateScheduleStatus(campaignId, clientId, CampaignStatus.PAUSED.name)
                                    pause(campaignId)
                                    logger.info(" Campaign Schedule paused $campaignId")
                                }
                            }
                        }
                        else -> {

                            if(nextExecutionDate==null) {
                                campaignRepository.updateScheduleStatus(campaignId, clientId, CampaignStatus.COMPLETED.name)
                                logger.info(" Campaign Schedule completed $campaignId")
                                auditCampaignLog(campaignId, clientId, status, action, jobActionStatus)
                            }
                        }
                    }
                }
                return
            }
            status == JobActionStatus.Status.OK -> {
                campaignRepository.updateScheduleStatus(campaignId, clientId, actionToCampaignStatus(actionPerformed).name)
                logger.info(" Campaign Schedule created $campaignId")
            }
            status == JobActionStatus.Status.DUPLICATE -> {
                //campaignRepository.updateScheduleStatus(campaignId, clientId, actionToCampaignStatus(actionPerformed).name)
                logger.error(" Campaign Schedule is duplicate for  $campaignId")
            }
            actionPerformed == JobDescriptor.Action.CREATE -> {
                campaignRepository.updateScheduleStatus(campaignId, clientId, CampaignStatus.ERROR.name)
                logger.error(" Campaign Schedule couldn't be created for $campaignId")
            }
            else -> {
                logger.error("  Schedule action couldn't be performed for $campaignId")
            }
        }


        auditCampaignLog(campaignId, clientId, status, action, jobActionStatus)
    }

    private fun auditCampaignLog(campaignId: Long, clientId: Long, status: JobActionStatus.Status, action: JobAction, jobActionStatus: JobActionStatus) {
        val auditLog = CampaignAuditLog()
        auditLog.campaignId = campaignId
        auditLog.clientID = clientId
        auditLog.status = status
        auditLog.action = action.action
        auditLog.message = jobActionStatus.message

        campaignAuditRepository.save(auditLog)
    }

    private fun createANewJobActionStatus(jobActionStatus: JobActionStatus):JobActionStatus{
        val jobstatus = JobActionStatus()
        val action = jobActionStatus.jobAction
        val clientId = action.clientId
        val campaignId = action.campaignId
        val campignName = action.campaignName
        val nextExecutionDate = action.nextTimeStamp
        with(jobstatus){
            jobAction = JobAction(clientId,campaignId,campignName,JobDescriptor.Action.AB_COMPLETED,nextExecutionDate)
            message = "Ab test completed for $campignName"
            status = JobActionStatus.Status.AB_COMPLETED
        }
        return jobstatus
    }

    fun actionToCampaignStatus(action: JobDescriptor.Action): CampaignStatus {
        return when (action) {
            JobDescriptor.Action.PAUSE -> CampaignStatus.PAUSED
            JobDescriptor.Action.FORCE_PAUSE -> CampaignStatus.FORCE_PAUSED
            JobDescriptor.Action.RESUME -> CampaignStatus.RESUMED
            JobDescriptor.Action.DELETE -> CampaignStatus.DELETED
            JobDescriptor.Action.CREATE -> CampaignStatus.CREATED
            JobDescriptor.Action.STOP -> CampaignStatus.STOPPED
            JobDescriptor.Action.NOTHING -> CampaignStatus.ERROR
            JobDescriptor.Action.COMPLETED -> CampaignStatus.COMPLETED
            JobDescriptor.Action.AB_COMPLETED -> CampaignStatus.AB_COMPLETED
        }
    }

    fun getScheduleError(campaignId: Long, clientId: Long): Optional<String> {
        val auditLog = campaignAuditRepository.findTopBycampaignIdAndClientIDOrderByIdDesc(campaignId, clientId)
        return auditLog.filter { it.status !in setOf(JobActionStatus.Status.OK, JobActionStatus.Status.DUPLICATE) && it.message.isNotEmpty() }.map { it.message }

    }


    fun getListOfCampaign(segmentId: Long): List<com.und.web.model.Campaign> {

        var clientId = AuthenticationUtils.clientID ?: throw AccessDeniedException("")
        var campaigns = campaignRepository.findByClientIDAndSegmentationID(clientId, segmentId)
        var listOfCampaign = mutableListOf<com.und.web.model.Campaign>()
        campaigns.forEach {
            var campaign = buildWebCampaign(it)
            listOfCampaign.add(campaign)
        }
        return listOfCampaign
    }

//    fun getClientFromAddressAndSrp(clientId: Long):ClientFromAddressAndSrp {
//        val emailSettings = clientSettingsEmailRepository.findByClientIdAndVerified(clientId, true)
//        var result = ClientFromAddressAndSrp()
//        if (emailSettings != null && emailSettings.isNotEmpty()) {
//            with(result) {
//                settings = buildClientFromAddressAndSrp(emailSettings)
//            }
//            return result
//        } else  return result
//    }
//
//    fun buildClientFromAddressAndSrp(result:List<ClientSettingsEmail>):Map<String,List<Long>>{
//        var map= mutableMapOf<String,MutableList<Long>>()
//        result.forEach {
//            if(map.containsKey(it.email)){
//                var list=map.get(it.email)!!
//                list?.add(it.serviceProviderId!!)
//                map.put(it.email!!,list)
//            }else{
//                map.put(it.email!!, mutableListOf(it.serviceProviderId!!))
//            }
//        }
//        return map
//    }

    fun getClientFromAddressAndSrp(clientId: Long): List<ClientEmailSettIdFromAddrSrp> {
//        var result=clientSettingsEmailRepository.joinClientEmailSettingAndServicePtoivder(clientId)
        return clientFromAddrAndSrpRepository.joinClientEmailSettingAndServicePtoivder(clientId)
    }

    private fun testCampaign(campaign: TestCampaign, clientId: Long) {
        logger.info("Sending Test Campaign for client $clientId to queue.")
        eventStream.outTestCampaign().send(MessageBuilder.withPayload(campaign).build())
    }

    fun runManualCampaign(campaignId: Long, clientId: Long,timeZone: ZoneId) {
        //On live campaign run type not playing any role.
        val campaign = campaignRepository.findByIdAndClientID(campaignId,clientId)
        campaign.ifPresent {
            val scheduleJson = it.schedule
            if(scheduleJson!=null && scheduleJson.isNotBlank()){
                val schedule = objectMapper.readValue(scheduleJson,Schedule::class.java)
                val (runRemaining,resumeOrNot)=runRemainingOrResumeOrNot(LocalDateTime.now(),schedule,timeZone)
                if(runRemaining){
                    logger.info("Manual campaign is triggered for clientId ${clientId}")
                    eventStream.triggerManualCampaign().send(MessageBuilder.withPayload(Pair(campaignId, clientId)).build())
                }
                if(resumeOrNot){
                    logger.info("Manual Type Campaign is resumed for clientId ${clientId}")
                    resume(campaignId)
                }else{
                    logger.info("Manual Type Campaign is completed for clientId ${clientId}")
                    campaignRepository.updateScheduleStatus(campaignId, clientId, CampaignStatus.COMPLETED.name)
                }

            }
        }

    }
    private fun runRemainingOrResumeOrNot(now:LocalDateTime,schedule: Schedule,timeZone: ZoneId):Pair<Boolean,Boolean>{
        var resumeRemaining:Boolean = false
        var resumeOrNot:Boolean = false
        schedule.oneTime?.let {
            resumeRemaining = true
        }
        schedule.multipleDates?.let {
            val executionDates = mutableListOf<LocalDateTime>()
            it.campaignDateTimeList.forEach {
                executionDates.add(it.toLocalDateTime())
            }
            //sort execution date
            Collections.sort(executionDates, object:Comparator<LocalDateTime>{
                override fun compare(o1: LocalDateTime?, o2: LocalDateTime?): Int {
                    return if(o1 !=null && o2!=null)  o1.compareTo(o2)  else 0
                }
            })
            return getStatusForMultiDate(executionDates,now)
        }
        schedule.recurring?.let {
            val endDate=it.scheduleEnd
            val startDate=it.scheduleStartDate!!
            when(endDate?.endType){
                ScheduleEndType.Occurrences -> {
                    val executionDates = getExecutionTimes(startDate=startDate,occurence = endDate.occurrences, cronExpression = it.cronExpression,endDate = null,timeZone = timeZone)
                    return getStatusForMultiDate(executionDates,now)
                }
                ScheduleEndType.EndsOnDate -> {
                    val endTime=endDate.endsOn
                    val executionTimes = getExecutionTimes(startDate = startDate,occurence = null ,cronExpression = it.cronExpression,endDate = endTime,timeZone = timeZone)
                    return getStatusForMultiDate(executionTimes,now)
                }
                ScheduleEndType.NeverEnd -> {
                    val executionTimes = getExecutionTimes(startDate = startDate,occurence = null ,cronExpression = it.cronExpression,endDate = null,neverEnd = true,timeZone = timeZone)
                    return getStatusForMultiDate(executionTimes,now)
                }
                else ->{}
            }
        }
        return Pair(resumeRemaining,resumeOrNot)
    }

    private fun getExecutionTimes(startDate:LocalDate,occurence:Int?,cronExpression:String,endDate:LocalDate?,neverEnd:Boolean=false,timeZone: ZoneId):List<LocalDateTime>{
        val cronParser = CronSequenceGenerator(cronExpression)
        val executionDates = mutableListOf<LocalDateTime>()

        occurence?.let {
            for ( i in 0..it-1 step 1){
                executionDates.add(dateToLocalDateTime(cronParser.next(localDateToDate(startDate,timeZone)),timeZone))
            }
        }

        endDate?.let {
            val enddate = localDateToDate(it,timeZone)
            var nextDate :Date = cronParser.next(localDateToDate(startDate,timeZone))
            do{
                executionDates.add(dateToLocalDateTime(nextDate,timeZone))
                nextDate = cronParser.next(nextDate)
            }while (nextDate.compareTo(enddate)<=0)
        }

        if(neverEnd){
            val firstDate= cronParser.next(localDateToDate(startDate,timeZone))
            executionDates.add(dateToLocalDateTime(firstDate,timeZone))
            val secondDate= cronParser.next(firstDate)
            executionDates.add(dateToLocalDateTime(secondDate,timeZone))
            executionDates.add(dateToLocalDateTime(cronParser.next(getMaxDateAfterYear(1,timeZone)),timeZone))
        }
        return executionDates
    }
    private fun getStatusForMultiDate(executionDates:List<LocalDateTime>,now: LocalDateTime):Pair<Boolean,Boolean>{
        var resumeRemaining:Boolean = false
        var resumeOrNot:Boolean = false

        if(executionDates.size==1){
            resumeRemaining = true
        }else{
            if(executionDates[1].compareTo(now)>0){
                resumeRemaining = true
                //TODO enhancement  ---> if next execution time is 1 hour after from now then user receive two
                // notification in short period of time one for ab test remaining and other from next execution time
                resumeOrNot = true
            }else if(executionDates.last().compareTo(now)>0){
                resumeOrNot = true
            }
        }

        return Pair(resumeRemaining,resumeOrNot)
    }

    // we are using server timezone when building jobdescriptor
    private fun localDateToDate(date: LocalDate, timeZone: ZoneId): Date {
        val defaultOffset = OffsetDateTime.now(timeZone).offset

        /** jdk 9
        val seconds = date.toEpochSecond(LocalTime.now(timeZone), defaultOffset)
        return Date.from(Instant.ofEpochSecond(seconds))
        */
        return Date.from(date.atStartOfDay().atZone(timeZone).toInstant())
    }

    fun dateToLocalDateTime(date: Date, timeZone: ZoneId): LocalDateTime {
        return LocalDateTime.ofInstant(date.toInstant(), timeZone)
    }

    fun localDateTimeToDate(date: LocalDateTime, timeZone: ZoneId): Date {
        val defaultOffset = OffsetDateTime.now(timeZone).offset
        val seconds = date.toEpochSecond(defaultOffset)
        return Date.from(Instant.ofEpochSecond(seconds))
    }

    fun getMaxDateAfterYear(year: Int, timeZone: ZoneId): Date {
        val localDateTime = LocalDateTime.now(timeZone).plusYears(year.toLong())
        return localDateTimeToDate(localDateTime, timeZone)
    }
    fun testPaging(page:Int):List<Campaign>{
        val defaultPageSize = 20
        var pagale = PageRequest.of(page,defaultPageSize)
        var result: Page<Campaign> = campaignRepository.findAll(pagale)
        print(result.totalPages)
        print(result.totalElements)
        return result.content
    }
}