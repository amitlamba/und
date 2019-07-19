package com.und.campaign.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.campaign.repository.jpa.*
import com.und.common.utils.BuildCampaignMessage
import com.und.campaign.utility.RedisUtilityService
import com.und.config.EventStream
import com.und.email.repository.jpa.ClientEmailSettingsRepository
import com.und.email.repository.jpa.ClientSettingsRepository
import com.und.email.repository.jpa.EmailTemplateRepository
import com.und.model.jpa.*
import com.und.model.jpa.Campaign
import com.und.model.mongo.EventUser
import com.und.model.mongo.EventUserRecord
import com.und.model.utils.*
import com.und.repository.jpa.security.UserRepository
import com.und.campaign.repository.mongo.EventUserRecordRepository
import com.und.campaign.feign.ReportServiceFeignClient
import com.und.campaign.model.CampaignUsers
import com.und.campaign.repository.mongo.CampaignUsersRepository
import com.und.campaign.repository.mongo.EventUserRepository
import com.und.service.SegmentService
import com.und.sms.repository.jpa.SmsTemplateRepository
import com.und.utils.loggerFor
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import javax.mail.internet.InternetAddress

@Service
class CampaignService {

    companion object {
        protected val logger = loggerFor(CampaignService::class.java)
        private val campaignReminderTemplateId = 7L
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
    private lateinit var reportServiceFeignClient: ReportServiceFeignClient

    @Autowired
    private lateinit var smsTemplateRepository: SmsTemplateRepository

    @Autowired
    private lateinit var eventUserRecordRepository: EventUserRecordRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var redisTemplalte: RedisTemplate<String, Int>

    @Autowired
    private lateinit var redisUtiltiyService: RedisUtilityService

    @Autowired
    private lateinit var clientRepository: ClientRepository

    @Autowired
    private lateinit var systemEmailRepository: SystemEmailRepository

    @Autowired
    private lateinit var campaignUsersRepository: CampaignUsersRepository

//    @Value(value = "\${und.system.email.setting.id}")
//    var clientEmailSettingId:Long?=null

//    @Value(value = "\${und.system.from.address}")
//    lateinit var systemFromAddress:String

    private var paginateNumber: Int = 10

    fun executeCampaign(campaignId: Long, clientId: Long) {
        //if client not trigger manual campaign then next time(if its multidate) we automatically send winner template.
        val campaign = findCampaign(campaignId, clientId)
        val executionId = ObjectId().toString()
        //TODO save campaaign triffer info
        when {
            (campaign.typeOfCampaign == TypeOfCampaign.AB_TEST) && (campaign.variants?.find { it.winner == true } == null) -> {
                runAbTest(executionId,campaign, clientId)
            }
            campaign.typeOfCampaign == TypeOfCampaign.SPLIT -> {
                runSplitCampaign(executionId,campaign, clientId)
            }
            else -> {
//                val usersData = getUsersData(campaign.segmentationID!!, clientId, campaign.campaignType)
//                usersData.forEach { user ->
//                    executeCampaignForUser(campaign, user, clientId)
//                }
                val users = usersInSegment(campaign.segmentationID!!, clientId).toList()
                sendUsersInGroupToMessagingService(executionId,campaignId, campaign.segmentationID!!, clientId, campaign.campaignType, users)
            }
        }
    }

    fun sendUsersInGroupToMessagingService(executionId: String,campaignId: Long, segmentId: Long, clientId: Long, campaignType: String, users: List<String>,groupStart:Int=1) {
        val noOfUsers = users.size
        val noOfGroups = noOfUsers.div(paginateNumber)
        //if(noOfUsers.rem(paginateNumber)>0) noOfGroups.inc()
        //TODO here max lose of 9 users
        for (groupId in groupStart..noOfGroups step 1) {
            val groupUser = users.subList(fromIndex = ((groupId - 1) * 10), toIndex = (groupId * 10))
            saveCampaignUsers(executionId,campaignId, clientId, segmentId, groupId.toLong(), groupUser, campaignType)
        }
    }

    fun sendToEmailMessagingService(infoModel: CampaignUsers) {
        //TODO send to kafka
    }

    fun sendToSmsMessagingService(infoModel: CampaignUsers) {
        //TODO send to kafka
    }

    fun sendToFcmMessagingService(infoModel: CampaignUsers) {
        //TODO send to kafka
    }

    fun saveCampaignUsers(executionId:String,campaignId: Long, clientId: Long, segmentId: Long, groupId: Long, users: List<String>, campaignType: String, templateId: Long? = null) {
        val userDoc = users.map {
            Document("userId", it)
        }
        //sending to messaging service
        val newTemplateId = when (CampaignType.valueOf(campaignType)) {
            CampaignType.EMAIL -> {
                if (templateId == null) {
                    val emailCampaign = emailCampaignRepository.findByCampaignId(campaignId)
                    if (!emailCampaign.isPresent) throw Exception("Email Campaign not exist for clientId ${clientId} and campaignId $campaignId}")
                    emailCampaign.get().templateId
                } else {
                    templateId
                }
            }
            CampaignType.PUSH_WEB -> {
                if (templateId == null) {
                    val webCampaign = webCampaignRepository.findByCampaignId(campaignId)
                    if (!webCampaign.isPresent) throw Exception("Web Campaign not exist for clientId ${clientId} and campaignId ${campaignId}")
                    webCampaign.get().templateId
                } else {
                    templateId
                }
            }
            CampaignType.PUSH_ANDROID -> {
                if (templateId == null) {
                    val androidCampaign = androidCampaignRepository.findByCampaignId(campaignId)
                    if (!androidCampaign.isPresent) throw Exception("Android Campaign not exist for clientId ${clientId} and campaignId ${campaignId}")
                    androidCampaign.get().templateId
                } else {
                    templateId
                }

            }
            CampaignType.PUSH_IOS -> {
                //TODO "send into ios message service queue"
                return
            }
            CampaignType.SMS -> {
                if (templateId == null) {
                    val smsCampaign = smsCampaignRepository.findByCampaignId(campaignId)
                    if (!smsCampaign.isPresent) throw Exception("Sms Campaign not exist for clientId ${clientId} and campaignId ${campaignId}")
                    smsCampaign.get().templateId
                } else {
                    templateId
                }

            }
        }
        val campaignUsers = CampaignUsers(campaignId, clientId, executionId, segmentId, groupId, newTemplateId!!, userDoc)
        sendToEmailMessagingService(campaignUsers)
        //save in mongo
        campaignUsersRepository.save(campaignUsers)
    }

    fun usersInSegment(segmentId: Long, clientId: Long): Set<String> {
        return segmentService.usersInSegment(segmentId, clientId)
    }

    fun runAbTest(executionId: String,campaign: Campaign, clientId: Long) {
        val unsentUserIds = multiTemplateCampaign(executionId,campaign, clientId)

        val record = EventUserRecord()
        with(record) {
            id = "${campaign.id}$clientId"
            this.clientId = clientId
            campaignId = campaign.id
            usersId = unsentUserIds
        }
        eventUserRecordRepository.save(record)

        val time = LocalDateTime.now().plusMinutes(campaign.abCampaign?.waitTime?.toLong() ?: 1)
        logger.info("Ab test complete and scheduled for time $time currrent time is ${LocalDateTime.now()}")
        val descriptor = buildJobDescriptor(campaign, "AB_${campaign.id}", JobDescriptor.Action.CREATE, time)
        eventStream.scheduleJobSend().send(MessageBuilder.withPayload(descriptor).build())
    }

    fun runSplitCampaign(executionId: String,campaign: Campaign, clientId: Long) {
        multiTemplateCampaign(executionId,campaign, clientId)
    }

    fun executeLiveCampaign(campaign: Campaign, clientId: Long, user: EventUser) {
        val present = communicationChannelPresent(campaign, user)
        if (present) {
            executeCampaignForUser(campaign, user, clientId)
        }

    }

    private fun multiTemplateCampaign(executionId: String,campaign: Campaign, clientId: Long): List<ObjectId> {
        val unsentUserIds = mutableListOf<ObjectId>()
        var listOfVariant = campaign.variants
        //val totalUsers = getUsersData(campaign.segmentationID!!, clientId, campaign.campaignType)
        val totalUsers = usersInSegment(campaign.segmentationID!!, clientId).toList()
        //Finding sample size of users
        val sampleUserSize = try {
            (totalUsers.size * (campaign.abCampaign?.sampleSize ?: 0)).div(100)
        } catch (ex: Exception) {
            0
        }
        var start = 1
        var startIndex = 1
        val listOfGroups = mutableListOf<Pair<Int, Int>>()
        listOfVariant?.forEach {
            //calculating the no of users for this variant from sample size
            val users = try {
                ((it.percentage ?: 0) * sampleUserSize).div(100)
            } catch (ex: Exception) {
                0
            }
            val noOfGroups = users.div(paginateNumber)
            for (i in start..(start + noOfGroups - 1) step 1) {
                listOfGroups.add(Pair(i, it.templateId!!))
            }
            startIndex = start + noOfGroups
            start = startIndex
        }
        listOfGroups.shuffle()
        listOfGroups.forEach {
            val groupUser = totalUsers.subList(fromIndex = ((it.first - 1) * 10), toIndex = (it.first * 10))
            saveCampaignUsers(executionId,campaign.id!!, clientId, campaign.segmentationID!!, it.first.toLong(), groupUser, campaign.campaignType, it.second.toLong())
        }
        totalUsers.listIterator(((startIndex - 1) * 10) + 1).forEach {
            unsentUserIds.add(ObjectId(it))
        }
        return unsentUserIds
    }

    fun executeCampaignForAb(campaignId: Long, clientId: Long) {
        val token = userRepository.findSystemUser().key ?: throw java.lang.Exception("Not Able to get system token.")
        val templateId = reportServiceFeignClient.getWinnerTemplate(campaignId, clientId, token, "ALL")
        //TODo update winner template in jpa
        val campaign = findCampaign(campaignId, clientId)
        when (campaign.abCampaign?.runType) {
            RunType.AUTO -> {
                executeRestOfCampaign(campaignId, clientId, campaign, templateId)
            }
            RunType.MANUAL -> {
                if (campaign.abCampaign?.remind ?: false) {
                    val client = clientRepository.findById(clientId).get()
                    val dataMap = mutableMapOf<String, Any>()
                    dataMap.put("name", client.name)
                    dataMap.put("phone", client.phone ?: "")
                    dataMap.put("email", client.email)
                    dataMap.put("firstname", client.firstname ?: "")
                    dataMap.put("lastname", client.lastname ?: "")
                    val systemEmail = systemEmailRepository.findByEmailTemplateId(campaignReminderTemplateId)
                    systemEmail?.let {
                        val clientEmailSettings = clientEmailSettingsRepository.findById(it.emailSettingId)
                        clientEmailSettings.ifPresent {
                            val email = Email(
                                    clientID = 1,
                                    fromEmailAddress = InternetAddress(it.email),
                                    toEmailAddresses = arrayOf(InternetAddress(client.email)),
                                    emailTemplateId = campaignReminderTemplateId,
                                    emailTemplateName = "campaignReminder",
                                    data = dataMap,
                                    clientEmailSettingId = it.id
                            )
                            eventStream.clientEmailOut().send(MessageBuilder.withPayload(email).build())
                        }

                    }

                }
            }
        }
    }

    fun executeCampaignForAbManual(campaignId: Long, clientId: Long) {
        val campaign = findCampaign(campaignId, clientId)
        val variant = campaign.variants?.find {
            it.winner
        }
        //todo if there is no variant then don't send.
        variant?: throw Exception("No winner variant is selected for client ${clientId} and campaignId ${campaignId}")
        executeRestOfCampaign(campaignId, clientId, campaign, variant.templateId?.toLong())
    }

    private fun executeRestOfCampaign(campaignId: Long, clientId: Long, campaign: Campaign, templateId: Long?) {
        val ids = eventUserRecordRepository.findById("$campaignId$clientId")
        ids.ifPresent {
            //find last group id
            val usersData = eventUserRepository.findAllById(clientId, ids.get().usersId)
            usersData.forEach { user ->
                executeCampaignForUser(campaign, user, clientId, templateId)
            }
        }
        //we are deleting the reset of users when campaign execute successfully for them.
        eventUserRecordRepository.deleteById("$campaignId$clientId")
    }


    fun executeSplitLiveCampaign(campaign: Campaign, clientId: Long, user: EventUser) {
//        // dump redis into mongo after some time interval.redis not support to dump its state in a database
//        // but we dump redis state in a file which is used by redis only.
//        val variants = redisTemplalte.opsForList().range("$clientId:${campaign.id}", 0, -1)//get list of template
//
//        if (variants == null || variants.isEmpty()) {
//            val listOfTemplateId = mutableListOf<Int>()
//            campaign.variants.forEach {
//                listOfTemplateId.add(it.templateId!!)
//                redisTemplalte.opsForHash<String, Int>().put("$clientId:${campaign.id}:${it.templateId}", "users", it.percentage!!*10.div(100))
//                redisTemplalte.opsForHash<String, Int>().put("$clientId:${campaign.id}:${it.templateId}", "count", it.percentage!!*10.div(100))
//            }
//            val templateId = campaign?.variants[0].templateId ?: return
//            redisTemplalte.opsForList().leftPushAll("$clientId:${campaign.id}", listOfTemplateId)
//            sendCampaign(clientId, campaign, templateId, user)
//
//        } else {
//            val templateId = variants.get(0)
//            sendCampaign(clientId, campaign, templateId, user)
//
//        }


    }

    fun newExecuteSplitLiveCampaign(campaign: Campaign, clientId: Long, user: EventUser) {
        //getting template id from queue
        var templateId = redisUtiltiyService.gettingFirstTemplateIdInQueue("$clientId:${campaign.id}")
        if (templateId == null) {
            val listOfTemplateId = mutableListOf<Int>()
            makingTemplateIdSequenceForRedis(campaign, listOfTemplateId)
            synchronized(this) {
                templateId = redisUtiltiyService.gettingFirstTemplateIdInQueue("$clientId:${campaign.id}")
                if (templateId == null) {
                    redisUtiltiyService.storingQueueOfTemplateIdToRedis("$clientId:${campaign.id}", listOfTemplateId)
                    templateId = redisUtiltiyService.gettingFirstTemplateIdInQueue("$clientId:${campaign.id}")
                }
            }

        }
        templateId?.let {
            try {
                executeCampaignForUser(campaign, user, clientId, it.toLong())
                //sending template id to end of queue
                redisUtiltiyService.addingTheTemplateIdToEndOfQueue("$clientId:${campaign.id}", it)

            } catch (ex: Exception) {
                //rollback operation adding the template id again in start of queue.
                redisTemplalte.opsForList().leftPush("$clientId:${campaign.id}", it)
            }
        }

    }

    /*
    * Here we are making a queue in redis which keep track of template id we are sending and next time we send
    * at a time we are storing 10 template id in queue. eg if we have two variant with 50% each then queue = [v1,v1,v1,v1,v1,v2,v2,v2,v2,v2]
    * */

    private fun makingTemplateIdSequenceForRedis(campaign: Campaign, listOfTemplateId: MutableList<Int>) {
        campaign.variants?.forEach {
            val size = (it.percentage!! * 10).div(100)
            for (i in size downTo 1 step 1) {
                listOfTemplateId.add(it.templateId ?: 0)
            }
        }
    }

    fun newExecuteAbTestLiveCampaign(campaign: Campaign, clientId: Long, user: EventUser) {

        val winnerTemplate = redisUtiltiyService.gettingWinnerTemplateForThisCampaign("$clientId:${campaign.id}:winner")
        if (winnerTemplate == null) {
            var templateId = redisTemplalte.opsForList().leftPop("$clientId:${campaign.id}")
            if (templateId == null) {
                //This block is executing mean this is the first user in our live segment.
                val listOfTemplateId = mutableListOf<Int>()
                makingTemplateIdSequenceForRedis(campaign, listOfTemplateId)
                redisUtiltiyService.storingSampleSizeToRedis("$clientId:${campaign.id}:sampleSize", campaign.abCampaign?.sampleSize
                        ?: 0)

                synchronized(this) {
                    templateId = redisUtiltiyService.gettingFirstTemplateIdInQueue("$clientId:${campaign.id}")
                    if (templateId == null) {
                        redisUtiltiyService.storingQueueOfTemplateIdToRedis("$clientId:${campaign.id}", listOfTemplateId)
                        templateId = redisUtiltiyService.gettingFirstTemplateIdInQueue("$clientId:${campaign.id}")
                    }
                }
            }
            templateId?.let {
                try {
                    executeCampaignForUser(campaign, user, clientId, it.toLong())
                    //right push that template id
                    redisUtiltiyService.addingTheTemplateIdToEndOfQueue("$clientId:${campaign.id}", it)
                    //decrement the user count of sample size
                    redisUtiltiyService.decreasingTheSampleSize("$clientId:${campaign.id}:sampleSize", -1)

                } catch (ex: Exception) {
                    //rollback operation adding the template id again in start of queue.
                    redisTemplalte.opsForList().leftPush("$clientId:${campaign.id}", it)
                }

                val sampleSize = redisUtiltiyService.gettingSampleSize("$clientId:${campaign.id}:sampleSize")
                if (sampleSize <= 0) {
                    //finding winner template
                    synchronized(this) {
                        //double checking
                        val winnerTemplateIdRedis = redisUtiltiyService.gettingWinnerTemplateForThisCampaign("$clientId:${campaign.id}:winner")
                        if (winnerTemplateIdRedis != null) executeCampaignForUser(campaign, user, clientId, winnerTemplateIdRedis.toLong())

                        val token = userRepository.findSystemUser().key
                                ?: throw java.lang.Exception("Not Able to get system token.")
                        val winnerTemplateId = reportServiceFeignClient.getWinnerTemplate(campaign.id!!, clientId, token, "ALL").toInt()
                        //Updating live campaign ab test status to completed
                        updateCampaignStatus(CampaignStatus.AB_COMPLETED, clientId, campaign.segmentationID ?: -1)
                        redisUtiltiyService.settingWinnerTemplateForThisCampaign("$clientId:${campaign.id}:winner", winnerTemplateId)

                    }

                }
            }


        } else {
            executeCampaignForUser(campaign, user, clientId, winnerTemplate.toLong())
        }
    }


    private fun communicationChannelPresent(campaign: Campaign, user: EventUser): Boolean {
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
                if (user.communication?.email == null || user.communication?.email?.dnd == true)
                    return //Local lambda return
                val email: Email = email(clientId, campaign, user, templateId)
                toKafka(email)
            }
            //check mode of communication is sms
            if (campaign.campaignType == "SMS") {

                if (user.communication?.mobile == null || user.communication?.mobile?.dnd == true)
                    return //Local lambda return
                val sms: Sms = sms(clientId, campaign, user, templateId)
                toKafka(sms)
            }
            //                check mode of communication is mobile push
            if (campaign.campaignType == "PUSH_ANDROID") {
                if (user.communication?.android == null || user.communication?.android?.dnd == true)
                    return //Local lambda return
                val notification = fcmAndroidMessage(clientId, campaign, user, templateId)
                toKafka(notification)
            }
            if (campaign.campaignType == "PUSH_WEB") {
                if (user.communication?.webpush == null || user.communication?.webpush?.dnd == true)
                    return //Local lambda return
                user.identity.webFcmToken?.forEach {
                    val notification = fcmWebMessage(clientId, campaign, user, it, templateId)
                    toKafka(notification)
                }
            }
            if (campaign.campaignType == "PUSH_IOS") {
                if (user.communication?.ios == null || user.communication?.ios?.dnd == true)
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
        val smsTemplate = if (smsTemplateId != null) smsTemplateRepository.findByIdAndClientID(smsTemplateId, clientId)
        else smsTemplateRepository.findByIdAndClientID(smsCampaign.get().templateId!!, clientId)
        smsTemplate
                ?: throw Exception("Sms Template for clientId ${clientId} , templateId ${smsCampaign.get().templateId} not exists.")
        return buildCampaignMessage.buildSms(clientId, campaign, user, smsCampaign.get(), smsTemplate)
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
        return segmentService.getUserData(segmentId, clientId, campaignType)
//        val segment = segmentService.getWebSegment(segmentId, clientId)
//        return segmentService.getUserData(segment, clientId, campaignType)
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
        //properties.typeOfCampaign = "AB_TEST"
//        runType?.let {
//            properties.runType = it.name
//        }


        val jobDetail = JobDetail()
        jobDetail.jobName = "${campaignId}-${campaignName}"
        jobDetail.jobGroupName = "${clientId}-${campaignId}"
        jobDetail.properties = properties
        return jobDetail
    }

    fun findCampaignById(id: Long): Campaign? {
        val campaign = campaignRepository.findById(id)
        return if (campaign.isPresent) campaign.get() else null
    }

}