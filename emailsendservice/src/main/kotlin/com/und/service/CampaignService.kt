package com.und.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.discovery.converters.Auto
import com.und.common.utils.BuildCampaignMessage
import com.und.config.EventStream
import com.und.model.jpa.*
import com.und.model.jpa.Campaign
import com.und.model.mongo.EventUser
import com.und.model.mongo.EventUserRecord
import com.und.model.utils.*
import com.und.repository.jpa.*
import com.und.repository.jpa.security.UserRepository
import com.und.repository.mongo.EventUserRecordRepository
import com.und.repository.mongo.EventUserRepository
import com.und.utils.loggerFor
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.mail.internet.InternetAddress
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

@Service
class CampaignService {

    companion object {
        protected val logger = loggerFor(CampaignService::class.java)
    }

    @Autowired
    private lateinit var campaignRepository: CampaignRepository
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    @Autowired
    private lateinit var segmentService: SegmentService
    @Autowired
    private lateinit var emailCampaignRepository: EmailCampaignRepository
    @Autowired
    private lateinit var androidCampaignRepository: AndroidCampaignRepository
    @Autowired
    private lateinit var webCampaignRepository: WebPushCampaignRepository
    @Autowired
    private lateinit var smsCampaignRepository: SmsCampaignRepository
    @Autowired
    private lateinit var clientEmailSettingsRepository: ClientEmailSettingsRepository
    @Autowired
    private lateinit var emailTemplateRepository: EmailTemplateRepository
    @Autowired
    private lateinit var eventStream: EventStream

    @Autowired
    private lateinit var eventUserRepository: EventUserRepository

    @Autowired
    private lateinit var clientSettingsRepository: ClientSettingsRepository

    @Autowired
    private lateinit var buildCampaignMessage: BuildCampaignMessage

    @Autowired
    private lateinit var segmentUserServiceClient: SegmentUserServiceClient

    @Autowired
    private lateinit var eventUserRecordRepository: EventUserRecordRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var redisTemplalte: RedisTemplate<String, Int>

    fun executeCampaign(campaignId: Long, clientId: Long) {
        val campaign = findCampaign(campaignId, clientId)
        when {
            (campaign.typeOfCampaign == TypeOfCampaign.AB_TEST )  && (campaign.variants.find { it.winner == true } == null) -> {
                runAbTest(campaign, clientId)
            }
            campaign.typeOfCampaign==TypeOfCampaign.SPLIT -> {
                runSplitCampaign(campaign, clientId)
            }
            else -> {
                val usersData = getUsersData(campaign.segmentationID!!, clientId, campaign.campaignType)
                usersData.forEach { user ->
                    executeCampaignForUser(campaign, user, clientId)
                }
            }
        }
    }

    fun runAbTest(campaign: Campaign, clientId: Long) {
        val ids = multiTemplateCampaign(campaign, clientId)

        val record = EventUserRecord()
        with(record) {
            id = "${campaign.id}$clientId"
            this.clientId = clientId
            campaignId = campaign.id
            usersId = ids
        }
        eventUserRecordRepository.save(record)

        val time = LocalDateTime.now().plusMinutes(campaign.abCampaign?.waitTime?.toLong() ?: 1)
        val descriptor = buildJobDescriptor(campaign, "AB_${campaign.id}", JobDescriptor.Action.CREATE, time)
        eventStream.scheduleJobSend().send(MessageBuilder.withPayload(descriptor).build())
    }

    fun runSplitCampaign(campaign: Campaign, clientId: Long) {
        //FIXME in case of behavioural type segment it may be possible that no of user at a time of campaign creation and execution time are diff.
        multiTemplateCampaign(campaign, clientId)
    }
    fun executeLiveCampaign(campaign: Campaign, clientId: Long, user: EventUser) {
        val present=communicationChannelPresent(campaign,user)
        if(present){
            executeCampaignForUser(campaign, user, clientId)
        }

    }

    private fun multiTemplateCampaign(campaign: Campaign, clientId: Long): List<ObjectId> {
        val ids = mutableListOf<ObjectId>()
        var listOfVariant = campaign.variants
        val usersData = getUsersData(campaign.segmentationID!!, clientId, campaign.campaignType)
        var start = 0
        var startIndex = 0
        listOfVariant.forEach {
            val users = it.users ?: 0
            for (i in start..(start + users-1) step 1) {
                executeCampaignForUser(campaign, usersData[i], clientId, it.templateId?.toLong())
            }
            startIndex = start + users
            start = users
        }

        usersData.listIterator(startIndex).forEach {
            ids.add(ObjectId(it.id))
        }
        return ids
    }

    fun executeCampaignForAb(campaignId: Long, clientId: Long) {
        val token = userRepository.findSystemUser().key ?: throw java.lang.Exception("Not Able to get system token.")
        val templateId = segmentUserServiceClient.getWinnerTemplate(campaignId, clientId, token, "ALL")
        val campaign = findCampaign(campaignId, clientId)
        when (campaign.abCampaign?.runType) {
            RunType.AUTO -> {
                val ids = eventUserRecordRepository.findById("$campaignId$clientId")
                ids.ifPresent {
                    val usersData = eventUserRepository.findAllById(clientId, ids.get().usersId)
                    usersData.forEach { user ->
                        executeCampaignForUser(campaign, user, clientId, templateId)
                    }
                }
                eventUserRecordRepository.deleteById("$campaignId$clientId")
            }
            RunType.MANUAL -> {
                //TODO if its manual then write end point to trigger rest of campaign manually.
                //Send reminder if ask
            }
        }

    }

    fun executeCampaignForAbManual(campaignId: Long, clientId: Long) {

        //TODO we should refactor this code
        val campaign = findCampaign(campaignId, clientId)
        val variant = campaign.variants.find {
            it.winner == true
        }
        val ids = eventUserRecordRepository.findById("$campaignId$clientId")
        ids.ifPresent {
            val usersData = eventUserRepository.findAllById(clientId, ids.get().usersId)
            usersData.forEach { user ->
                executeCampaignForUser(campaign, user, clientId, variant?.templateId?.toLong())
            }
        }
        //TODO we are deleting when campaign execute successfully.if any error occur then we dont have any record to how many users we send campaign.
        eventUserRecordRepository.deleteById("$campaignId$clientId")
    }



    fun executeSplitLiveCampaign(campaign: Campaign, clientId: Long, user: EventUser) {
        //TODO handle ab test campaign associated with live segment
        //TODo handle run type also but in live segment run type not play role.
        //FIXME dump redis into mongo after some time interval.redis not support to dump its state in a database
        // but we dump redis state in a file which is used by redis only.
        val variants = redisTemplalte.opsForList().range("$clientId:${campaign.id}", 0, -1)//get list of template
        if (variants == null || variants.isEmpty()) {
            val listOfTemplateId = mutableListOf<Int>()
            campaign.variants.forEach {
                listOfTemplateId.add(it.templateId!!)
                redisTemplalte.opsForHash<String, Int>().put("$clientId:${campaign.id}:${it.templateId}", "users", it.users)
                redisTemplalte.opsForHash<String, Int>().put("$clientId:${campaign.id}:${it.templateId}", "count", it.counter)
            }
            val templateId = campaign.variants[0].templateId ?: return
            redisTemplalte.opsForList().leftPushAll("$clientId:${campaign.id}", listOfTemplateId)
            sendCampaign(clientId, campaign, templateId, user)

        } else {
            val templateId = variants.get(0)
            sendCampaign(clientId, campaign, templateId, user)

        }


    }

    private fun sendCampaign(clientId: Long, campaign: Campaign, templateId: Int, user: EventUser) {
        //TODO make it thread safe.
        try {
            redisTemplalte.multi()      //starting transaction
            var counter = redisTemplalte.opsForHash<String, Int>().get("$clientId:${campaign.id}:${templateId}", "count")
            val newCounter = AtomicInteger(counter)
            // sending notification to this template id
            executeCampaignForUser(campaign, user, clientId, templateId.toLong())
            counter--
            //TODO we can replace it with atomic operation
            //redisTemplalte.opsForHash<String,Int>().increment("$clientId:${campaign.id}:${templateId}","count",-1)
            if (counter == 0) {
                redisTemplalte.opsForList().leftPop("$clientId:${campaign.id}")
                redisTemplalte.opsForList().rightPush("$clientId:${campaign.id}", templateId)
            } else {
                redisTemplalte.opsForHash<String, Int>().put("$clientId:${campaign.id}:${templateId}", "count", counter)
            }

            redisTemplalte.exec()   //committing transaction
        } catch (ex: java.lang.Exception) {
            redisTemplalte.discard()    //rollback discard all changes.
        }
    }

    fun executeAbTestLiveCampaign(campaign: Campaign, clientId: Long, user: EventUser) {
        //TODO handle split campaign associated with live segment
        // TODO update when complete test campaign.
        val variants = redisTemplalte.opsForList().range("$clientId:${campaign.id}", 0, -1) //get list of template
        val winnerTemplate = redisTemplalte.opsForValue().get("$clientId:${campaign.id}:winner")
        if (variants == null) {
            val listOfTemplateId = mutableListOf<Int>()
            campaign.variants.forEach {
                listOfTemplateId.add(it.templateId!!)
                redisTemplalte.opsForHash<String, Int>().putAll("$clientId:${campaign.id}:${it.templateId}",
                        mapOf(Pair("users", it.users), Pair("count", it.counter), Pair("percentage", it.percentage)))
            }
            var templateId = campaign.variants[0].templateId ?: return
            redisTemplalte.opsForList().leftPushAll("$clientId:${campaign.id}", listOfTemplateId)
            sendAbLiveCampaign(clientId, campaign, templateId, user)
        } else if (winnerTemplate != null) {
            //get winner template and send notification
            executeCampaignForUser(campaign, user, clientId, winnerTemplate.toLong())
        } else {
            val templateId = variants.get(0)

            sendAbLiveCampaign(clientId, campaign, templateId, user)
        }
    }

    private fun sendAbLiveCampaign(clientId: Long, campaign: Campaign, templateId: Int, user: EventUser) {
        val v = redisTemplalte.opsForHash<String, Int>().entries("$clientId:${campaign.id}:${templateId}")

        var counter = v["count"] ?: 0
        var users = v["users"] ?: 0

        executeCampaignForUser(campaign, user, clientId, templateId.toLong())
        users--
        counter--
        if (counter == 0 && users != 0) {
            redisTemplalte.opsForList().leftPop("$clientId:${campaign.id}")
            redisTemplalte.opsForList().rightPush("$clientId:${campaign.id}", templateId)
            redisTemplalte.opsForHash<String, Int>().put("$clientId:${campaign.id}:${templateId}", "count", (v["percentage"]?.div(10))
                    ?: 0)
            redisTemplalte.opsForHash<String, Int>().put("$clientId:${campaign.id}:${templateId}", "users", users)
        } else if (users == 0) {
            val token = userRepository.findSystemUser().key
                    ?: throw java.lang.Exception("Not Able to get system token.")
            val templateId = segmentUserServiceClient.getWinnerTemplate(campaign.id!!, clientId, token, "ALL")
            redisTemplalte.opsForValue().set("$clientId:${campaign.id}:winner", templateId.toInt())
            redisTemplalte.opsForList().leftPop("$clientId:${campaign.id}")
        } else {
            redisTemplalte.opsForHash<String, Int>().putAll("$clientId:${campaign.id}:${templateId}",
                    mapOf(Pair("users", users), Pair("count", counter)))
        }
    }

    private fun communicationChannelPresent(campaign: Campaign,user: EventUser):Boolean{
        return when (campaign.campaignType) {
            "EMAIL" -> {
                user.identity.email != null
            }
            "SMS" -> {
                user.identity.mobile != null
            }
            "PUSH_ANDROID" -> {
                user.identity.androidFcmToken != null
            }
            "PUSH_WEB" -> {
                user.identity.webFcmToken != null
            }
            "PUSH_IOS" -> {
                user.identity.iosFcmToken != null
            }
            else -> false
        }
    }
    private fun findCampaign(campaignId: Long, clientId: Long): Campaign {
        val campaignOption = campaignRepository.findById(campaignId)
        return campaignOption.orElseThrow { IllegalStateException("campaign not found for campaign id $campaignId and client $clientId") }
    }

    fun findLiveSegmentCampaign(segmentId: Long, clientId: Long): List<Campaign> {
        //FIXME if client panel and email send service are running in diff timezone then there is exact time matching problem.
        return campaignRepository.getCampaignByClientIDAndSegmentationIDAndStartDateBeforeAndEndDateAfter(segmentId, clientId)
    }

    fun findAllLiveSegmentCampaignBySegmentId(segmentId: Long, clientId: Long): List<Campaign> {
        //FIXME if client panel and email send service are running in diff timezone then there is exact time matching problem.
        return campaignRepository.findAllByClientIDAndSegmentationIDAndStartDateBefore(segmentId, clientId)
    }

    private fun executeCampaignForUser(campaign: Campaign, user: EventUser, clientId: Long, templateId: Long? = null) {
        try {
            //TODO: filter out unsubscribed and blacklisted users
            //TODO: How to skip transactional Messages
            //check mode of communication is email
            if (campaign.campaignType == "EMAIL") {
                //TODO check whats happen if email not exists
                if (user.communication?.email?.dnd == true)
                    return //Local lambda return
                val email: Email = email(clientId, campaign, user, templateId)
                toKafka(email)
            }
            //check mode of communication is sms
            if (campaign.campaignType == "SMS") {

                if (user.communication?.mobile?.dnd == true)
                    return //Local lambda return
                val sms: Sms = sms(clientId, campaign, user, templateId)
                toKafka(sms)
            }
            //                check mode of communication is mobile push
            if (campaign.campaignType == "PUSH_ANDROID") {
                if (user.communication?.android?.dnd == true)
                    return //Local lambda return
                val notification = fcmAndroidMessage(clientId, campaign, user, templateId)
                toKafka(notification)
            }
            if (campaign.campaignType == "PUSH_WEB") {
                if (user.communication?.webpush?.dnd == true)
                    return //Local lambda return
                user.identity.webFcmToken?.forEach {
                    val notification = fcmWebMessage(clientId, campaign, user, it, templateId)
                    toKafka(notification)
                }
            }
            if (campaign.campaignType == "PUSH_IOS") {
                if (user.communication?.ios?.dnd == true)
                    return //Local lambda return
                val notification = fcmIosMessage(clientId, campaign, user)
                toKafka(notification)
            }
        } catch (ex: Exception) {
            logger.error(ex.message)

        } finally {

        }
    }

    private fun sms(clientId: Long, campaign: Campaign, user: EventUser, smsTemplateId: Long? = null): Sms {
        val smsCampaign = smsCampaignRepository.findByCampaignId(campaign.id!!)
        if (!smsCampaign.isPresent) throw Exception("Sms Campaign not exist for clientId ${clientId} and campaignId ${campaign.id}")
        val smsTemplate = if (smsTemplateId != null) emailTemplateRepository.findByIdAndClientID(smsTemplateId, clientId)
        else emailTemplateRepository.findByIdAndClientID(smsCampaign.get().templateId!!, clientId)
        if (!smsTemplate.isPresent) throw Exception("Sms Template for clientId ${clientId} , templateId ${smsCampaign.get().templateId} not exists.")
        return buildCampaignMessage.buildSms(clientId, campaign, user, smsCampaign.get(), smsTemplate.get())
    }


    private fun email(clientId: Long, campaign: Campaign, user: EventUser, templateId: Long? = null): Email {
        try {
            val emailCampaign = emailCampaignRepository.findByCampaignId(campaign.id!!)
            if (!emailCampaign.isPresent) throw Exception("Email Campaign not exist for clientId ${clientId} and campaignId ${campaign.id}")

            val emailTemplate = if (templateId != null) emailTemplateRepository.findByIdAndClientID(templateId, clientId)
            else emailTemplateRepository.findByIdAndClientID(emailCampaign.get().templateId!!, clientId)
            if (!emailTemplate.isPresent) throw Exception("Email Template for clientId ${clientId} , templateId ${emailCampaign.get().templateId} not exists.")
//        val clientEmailSettings= clientEmailSettingsRepository.
//                findByClientIdAndEmailAndServiceProviderId(clientId,campaign.fromUser!!,campaign.serviceProviderId!!)
//        if (!clientEmailSettings.isPresent) throw Exception("Client Email Settings not present for client ${clientId} fromAddress ${campaign.fromUser} sp ${campaign.serviceProviderId}")
            return buildCampaignMessage.buildEmail(clientId, campaign, user, emailCampaign.get(), emailTemplate.get())
        } catch (ex: Exception) {
            throw ex
        }
    }


    private fun fcmAndroidMessage(clientId: Long, campaign: Campaign, user: EventUser, templateId: Long? = null): FcmMessage {
        //Todo passing data model
        val androidCampaign = androidCampaignRepository.findByCampaignId(campaign.id!!)
        if (!androidCampaign.isPresent) throw Exception("Android Campaign not exist for clientId ${clientId} and campaignId ${campaign.id}")
        return buildCampaignMessage.buildAndroidFcmMessage(clientId, androidCampaign.get(), user, campaign, templateId)
    }


    private fun fcmWebMessage(clientId: Long, campaign: Campaign, user: EventUser, token: String, templateId: Long? = null): FcmMessage {
        val webPushCampaign = webCampaignRepository.findByCampaignId(campaign.id!!)
        if (!webPushCampaign.isPresent) throw Exception("Web Campaign not exist for clientId ${clientId} and campaignId ${campaign.id}")
        return buildCampaignMessage.buildWebFcmMessage(clientId, webPushCampaign.get(), token, campaign, user, templateId)
    }


    private fun fcmIosMessage(clientId: Long, campaign: Campaign, user: EventUser): FcmMessage {
//        val iosCampaign =iosCampaignRepository.findByCampaignId(campaign.id!!)
        return buildCampaignMessage.buildIosFcmMessage(clientId, user, campaign)
    }


    fun updateCampaignStatus(status: CampaignStatus, clientId: Long, segmentId: Long) {
        campaignRepository.updateStatusOfCampaign(status.name, segmentId, clientId)
    }

    fun updateCampaignStatusByCampaignId(status: CampaignStatus, clientId: Long, campaignId: Long) {
        campaignRepository.updateStatusOfCampaignById(status.name, campaignId, clientId)
    }

    fun getUsersData(segmentId: Long, clientId: Long, campaignType: String): List<EventUser> {
        val segment = segmentService.getWebSegment(segmentId, clientId)
        return segmentService.getUserData(segment, clientId, campaignType)
    }

    fun toKafka(fcmMessage: FcmMessage) {
        eventStream.fcmEventSend().send(MessageBuilder.withPayload(fcmMessage).build())
    }

    fun toKafka(email: Email): Boolean =
            eventStream.emailEventSend().send(MessageBuilder.withPayload(email).build())


    fun toKafka(sms: Sms): Boolean =
            eventStream.smsEventSend().send(MessageBuilder.withPayload(sms).build())


    private fun buildJobDescriptor(campaign: Campaign, name: String, action: JobDescriptor.Action, time: LocalDateTime): JobDescriptor {

        fun buildTriggerDescriptor(time: LocalDateTime): TriggerDescriptor {
            val triggerDescriptor = TriggerDescriptor()
            with(triggerDescriptor) {
                fireTime = time
            }
            return triggerDescriptor
        }

        val jobDescriptor = JobDescriptor()
        jobDescriptor.timeZoneId = ZoneId.of(clientSettingsRepository.findByClientID(campaign.clientID!!)?.timezone
                ?: "UTC")
        jobDescriptor.clientId = campaign.clientID.toString()
        jobDescriptor.action = action
        jobDescriptor.jobDetail = buildJobDetail(campaign.id.toString(), name, jobDescriptor.clientId, campaign.abCampaign?.runType)

        val triggerDescriptors = arrayListOf<TriggerDescriptor>()
        triggerDescriptors.add(buildTriggerDescriptor(time))
        jobDescriptor.triggerDescriptors = triggerDescriptors
        return jobDescriptor
    }

    private fun buildJobDetail(campaignId: String, campaignName: String, clientId: String, runType: RunType? = null): JobDetail {
        val properties = CampaignJobDetailProperties()
        properties.campaignName = campaignName
        properties.campaignId = campaignId
        properties.abCompleted = "COMPLETED"
        properties.typeOfCampaign = "AB_TEST"
        runType?.let {
            properties.runType = it.name
        }


        val jobDetail = JobDetail()
        jobDetail.jobName = "${campaignId}-${campaignName}"
        jobDetail.jobGroupName = "${clientId}-${campaignId}"
        jobDetail.properties = properties
        return jobDetail
    }

    fun findCampaignByIds(ids:List<Long>):List<Campaign>{
        return campaignRepository.findAllById(ids)
    }

}