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
import com.und.campaign.model.CampaignTriggerInfo
import com.und.campaign.model.CampaignUsers
import com.und.campaign.model.ExecutionStatus
import com.und.campaign.repository.mongo.CampaignUsersRepository
import com.und.campaign.repository.mongo.EventUserRepository
import com.und.repository.jpa.CampaignTriggerInfoRepository
import com.und.service.SegmentService
import com.und.sms.repository.jpa.SmsTemplateRepository
import com.und.utils.loggerFor
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
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

    @Autowired
    private lateinit var campaignTriggerInfoRepository: CampaignTriggerInfoRepository

//    @Value(value = "\${und.system.email.setting.id}")
//    var clientEmailSettingId:Long?=null

//    @Value(value = "\${und.system.from.address}")
//    lateinit var systemFromAddress:String
    @Value(value="\${und.system.paginationNo}")
    private var paginateNumber: Int?=null

    fun executeCampaign(campaignId: Long, clientId: Long) {
        //if client not trigger manual campaign then next time(if its multidate) we automatically send winner template.
        val campaign = findCampaign(campaignId, clientId)
        val executionId = ObjectId().toString()
        updateCampaignTriggerInfo(campaignId, executionId, clientId)
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

    private fun updateCampaignTriggerInfo(campaignId: Long, executionId: String, clientId: Long) {
        logger.info("updating campaign trigger info for clientId $clientId executionId $executionId campaignId $campaignId")
        val campaignTriggerInfo = campaignTriggerInfoRepository.findByCampaignId(campaignId)
        if (campaignTriggerInfo.isPresent) {
            val cTInfo = campaignTriggerInfo.get()
            val newExecutionStatus = ExecutionStatus()
            with(newExecutionStatus) {
                this.executionId = executionId
                this.executionTime = LocalDateTime.now(ZoneId.systemDefault())
            }
            val executionStatus = cTInfo.executionStatus.toMutableList()
            executionStatus.add(newExecutionStatus)
            cTInfo.executionStatus = executionStatus
            campaignTriggerInfoRepository.save(cTInfo)
        } else {
            val newCampaignTriggerInfo = CampaignTriggerInfo()
            val executionStatus = ExecutionStatus()
            with(executionStatus) {
                this.executionId = executionId
                this.executionTime = LocalDateTime.now(ZoneId.systemDefault())
            }
            val list = ArrayList<ExecutionStatus>()
            list.add(executionStatus)

            with(newCampaignTriggerInfo) {
                this.campaignId = campaignId
                this.clientId = clientId
                this.error = false
                //this.executionStatus.plus(executionStatus)
            }
            newCampaignTriggerInfo.executionStatus = list
            campaignTriggerInfoRepository.save(newCampaignTriggerInfo)
        }
        logger.info("updating campaign trigger info for clientId $clientId executionId $executionId campaignId $campaignId is successful.")
    }

    fun sendUsersInGroupToMessagingService(executionId: String,campaignId: Long, segmentId: Long, clientId: Long, campaignType: String, users: List<String>) {
        val noOfUsers = users.size
        val noOfGroups = noOfUsers.div(paginateNumber?:10)
        val remainder = noOfUsers.rem(paginateNumber?:10)
        //TODO here max lose of 9 users
        for (groupId in 1..noOfGroups step 1) {
            val groupUser = users.subList(fromIndex = ((groupId - 1) * (paginateNumber?:10)), toIndex = (groupId * (paginateNumber?:10)))
            saveCampaignUsers(executionId,campaignId, clientId, segmentId, groupId.toLong(), groupUser, campaignType)
        }
        if(remainder>0){
            val groupUser = users.subList(fromIndex =(noOfGroups*(paginateNumber?:10)) , toIndex = (noOfGroups*(paginateNumber?:10))+remainder)
            saveCampaignUsers(executionId,campaignId, clientId, segmentId, (noOfGroups+1).toLong(), groupUser, campaignType)
        }
    }

    fun sendToEmailMessagingService(infoModel: CampaignUsers) {
        eventStream.emailEventSend().send(MessageBuilder.withPayload(infoModel).build())
    }

    fun sendToSmsMessagingService(infoModel: CampaignUsers) {
        eventStream.smsEventSend().send(MessageBuilder.withPayload(infoModel).build())
    }

    fun sendToFcmMessagingService(infoModel: CampaignUsers) {
        eventStream.fcmEventSend().send(MessageBuilder.withPayload(infoModel).build())
    }

    /**
     * This function save and send the users in group to messaging service.
     */
    fun saveCampaignUsers(executionId:String,campaignId: Long, clientId: Long, segmentId: Long, groupId: Long,
                          users: List<String>, campaignType: String, templateId: Long? = null,
                          usersPartOfAbTest:Boolean = false,isAbType:Boolean = false) {
        logger.debug("saving and sending campaign users in group for groupId ${groupId} campaignId ${campaignId} clientId ${clientId} " +
                "templateId ${templateId} campaignType $campaignType isAbType $isAbType ")
        val userDoc = users.map {
            Document("userId", it)
        }
        val campaignUsers = when (CampaignType.valueOf(campaignType)) {
            CampaignType.EMAIL -> {
                val newTemplateId =if (templateId == null) {
                    val emailCampaign = emailCampaignRepository.findByCampaignId(campaignId)
                    if (!emailCampaign.isPresent) throw Exception("Email Campaign not exist for clientId ${clientId} and campaignId $campaignId}")
                    emailCampaign.get().templateId
                } else {
                    templateId
                }
                val campaignUsers = CampaignUsers(campaignId, clientId, executionId, segmentId, groupId, newTemplateId!!, userDoc,usersPartOfAbTest,isAbType)
                sendToEmailMessagingService(campaignUsers)
                campaignUsers
            }
            CampaignType.PUSH_WEB -> {
                val newTemplateId =if (templateId == null) {
                    val webCampaign = webCampaignRepository.findByCampaignId(campaignId)
                    if (!webCampaign.isPresent) throw Exception("Web Campaign not exist for clientId ${clientId} and campaignId ${campaignId}")
                    webCampaign.get().templateId
                } else {
                    templateId
                }
                val campaignUsers = CampaignUsers(campaignId, clientId, executionId, segmentId, groupId, newTemplateId!!, userDoc,usersPartOfAbTest,isAbType)
                sendToFcmMessagingService(campaignUsers)
                campaignUsers
            }
            CampaignType.PUSH_ANDROID -> {
                val newTemplateId =if (templateId == null) {
                    val androidCampaign = androidCampaignRepository.findByCampaignId(campaignId)
                    if (!androidCampaign.isPresent) throw Exception("Android Campaign not exist for clientId ${clientId} and campaignId ${campaignId}")
                    androidCampaign.get().templateId
                } else {
                    templateId
                }
                val campaignUsers = CampaignUsers(campaignId, clientId, executionId, segmentId, groupId, newTemplateId!!, userDoc,usersPartOfAbTest,isAbType)
                sendToFcmMessagingService(campaignUsers)
                campaignUsers

            }
            CampaignType.PUSH_IOS -> {
                //TODO "send into ios message service queue"
                return
            }
            CampaignType.SMS -> {
                val newTemplateId = if (templateId == null) {
                    val smsCampaign = smsCampaignRepository.findByCampaignId(campaignId)
                    if (!smsCampaign.isPresent) throw Exception("Sms Campaign not exist for clientId ${clientId} and campaignId ${campaignId}")
                    smsCampaign.get().templateId
                } else {
                    templateId
                }
                val campaignUsers = CampaignUsers(campaignId, clientId, executionId, segmentId, groupId, newTemplateId!!, userDoc,usersPartOfAbTest,isAbType)
                sendToSmsMessagingService(campaignUsers)
                campaignUsers
            }
        }

        campaignUsersRepository.save(campaignUsers)
        logger.info("saved and send campaign users for campaign $campaignId , client $clientId , group $groupId , templateId ${campaignUsers.templateId}")
        logger.debug("saving and sending campaign users in group for groupId ${groupId} campaignId ${campaignId} " +
                "clientId ${clientId} templateId ${templateId} campaignType $campaignType isAbType $isAbType")
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
        logger.info("sending ab test for clientId $clientId campaignId ${campaign.id} executionId $executionId ")
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
        val remainderGroups = mutableListOf<Pair<Int,Int>>()
        listOfVariant?.forEach {
            //calculating the no of users for this variant from sample size
            val users = try {
                ((it.percentage ?: 0) * sampleUserSize).div(100)
            } catch (ex: Exception) {
                0
            }
            val noOfGroups = users.div(paginateNumber?:10)
            val remainder = users.rem(paginateNumber?:10)
            for (i in start..(start + noOfGroups - 1) step 1) {
                listOfGroups.add(Pair(i, it.templateId!!))
            }
            if(remainder>0){
                remainderGroups.add(Pair(remainder,it.templateId!!))
            }
            startIndex = start + noOfGroups
            start = startIndex
        }
        listOfGroups.shuffle()
        logger.debug("campaign Ab test clientId $clientId campaignid ${campaign.id} totalUsers $totalUsers " +
                "total sample users $sampleUserSize groups ${listOfGroups.size} unsentUser startIndex $startIndex ....")
        listOfGroups.forEach {
            val groupUser = totalUsers.subList(fromIndex = ((it.first - 1) * (paginateNumber?:10)), toIndex = (it.first * (paginateNumber?:10)))
            logger.debug(".... campaign Ab test clientId $clientId campaignid ${campaign.id} groupId ${it.first} " +
                    "fromIndex ${((it.first - 1) * (paginateNumber?:10))} toIndex ${(it.first * (paginateNumber?:10))}")
            saveCampaignUsers(executionId,campaign.id!!, clientId, campaign.segmentationID!!, it.first.toLong(), groupUser, campaign.campaignType, it.second.toLong(),true,true)
        }
        var remainderIndex = (startIndex-1)*(paginateNumber?:10)
        val totalGroups = listOfGroups.size
        remainderGroups.forEach {
            val remainderUsers = it.first
            val groupUser = totalUsers.subList(fromIndex = remainderIndex, toIndex = (remainderIndex+remainderUsers))
            logger.debug(".... campaign Ab test clientId $clientId campaignid ${campaign.id} groupId ${it.first} " +
                    "fromIndex ${remainderIndex} toIndex ${remainderIndex+remainderUsers}")
            saveCampaignUsers(executionId,campaign.id!!, clientId, campaign.segmentationID!!, totalGroups.inc().toLong(), groupUser, campaign.campaignType, it.second.toLong(),true,true)
            remainderIndex+=remainderUsers
        }
        totalUsers.listIterator(remainderIndex).forEach {
            unsentUserIds.add(ObjectId(it))
        }
        return unsentUserIds
    }

    fun executeCampaignForAb(campaignId: Long, clientId: Long) {
        val token = userRepository.findSystemUser().key ?: throw java.lang.Exception("Not Able to get system token.")
        val templateId = reportServiceFeignClient.getWinnerTemplate(campaignId, clientId, token, "ALL")
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
        variant?: throw Exception("No winner variant is selected for client ${clientId} and campaignId ${campaignId}")
        executeRestOfCampaign(campaignId, clientId, campaign, variant.templateId?.toLong())
    }

    private fun executeRestOfCampaign(campaignId: Long, clientId: Long, campaign: Campaign, templateId: Long?) {
        logger.info("executing rest of campaign for clientId $clientId campaignId $campaignId")
        val ids = eventUserRecordRepository.findById("$campaignId$clientId")
        ids.ifPresent {
            val campaignTriggerInfo = campaignTriggerInfoRepository.findById(campaignId)
            if(campaignTriggerInfo.isPresent){
                val executionId = getLastExecutionId(campaignTriggerInfo.get())
                logger.debug(""" campaign trigger info for clientId $clientId campaignId $campaignId
                    ${campaignTriggerInfo.get()} and last execution id is $executionId ....
                """.trimMargin())
                val campaignUsers = campaignUsersRepository.findByClientIdAndCampaignIdAndExecutionId(clientId,campaignId,executionId)
                val groupEndIndex = campaignUsers.size
                val noOfUsers = it.usersId.size
                val noOfGroups = noOfUsers.div(paginateNumber?:10)
                val remainder = noOfUsers.rem(paginateNumber?:10)
                //if(noOfUsers.rem(paginateNumber)>0) noOfGroups.inc()
                //TODO here max lose of 9 users
                logger.debug(".... group take part in ab test $groupEndIndex rest of users $noOfUsers totalgroup ${groupEndIndex+noOfGroups} ")
                for (groupId in groupEndIndex+1..groupEndIndex+noOfGroups step 1) {
                    val groupUser = it.usersId.subList(fromIndex = ((groupId - 1) * (paginateNumber?:10)), toIndex = (groupId * (paginateNumber?:10))).map { it.toHexString() }
                    saveCampaignUsers(executionId,campaignId, clientId, campaign.segmentationID!!, groupId.toLong(), groupUser, campaign.campaignType,templateId = templateId,isAbType = true)
                }
                if(remainder>0){
                    val groupUser = it.usersId.subList(fromIndex =(noOfGroups*(paginateNumber?:10)) , toIndex = (noOfGroups*(paginateNumber?:10))+remainder).map { it.toHexString() }
                    saveCampaignUsers(executionId,campaignId, clientId, campaign.segmentationID!!, (noOfGroups+1).toLong(), groupUser, campaign.campaignType)
                }
            }

//            val usersData = eventUserRepository.findAllById(clientId, ids.get().usersId)
//            usersData.forEach { user ->
//                executeCampaignForUser(campaign, user, clientId, templateId)
//            }

        }
        logger.info("deleting rest of users for clientId $clientId campaignId $campaignId")
        eventUserRecordRepository.deleteById("$campaignId$clientId")
    }

    private fun getLastExecutionId(campaignTriggerInfo: CampaignTriggerInfo):String{
        var executionStatus = campaignTriggerInfo.executionStatus
        executionStatus = executionStatus.sortedWith( Comparator { obj1, obj2 ->
            obj1.executionTime.compareTo(obj2.executionTime)
        })
        return executionStatus.last().executionId
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

    private fun executeCampaignForUser(campaign: Campaign, user: EventUser, clientId: Long, templateId: Long?=null) {
        try {
            //TODO: filter out unsubscribed and blacklisted users
            //TODO: How to skip transactional Messages
            //check mode of communication is email
            if (campaign.campaignType == "EMAIL") {
                if (user.communication?.email == null || user.communication?.email?.dnd == true)
                    return //Local lambda return
                eventStream.outEmailLiveCampaign().
                        send(MessageBuilder.withPayload(LiveCampaignTriggerInfo(campaign.id!!,clientId,user.id!!,templateId)).build())


//                val email: Email = email(clientId, campaign, user, templateId)
//                toKafka(email)
            }
            //check mode of communication is sms
            if (campaign.campaignType == "SMS") {

                if (user.communication?.mobile == null || user.communication?.mobile?.dnd == true)
                    return //Local lambda return
                eventStream.outSmsLiveCampaign().
                        send(MessageBuilder.withPayload(LiveCampaignTriggerInfo(campaign.id!!,clientId,user.id!!,templateId)).build())
//                val sms: Sms = sms(clientId, campaign, user, templateId)
//                toKafka(sms)
            }
            //                check mode of communication is mobile push
            if (campaign.campaignType == "PUSH_ANDROID") {
                if (user.communication?.android == null || user.communication?.android?.dnd == true)
                    return //Local lambda return
                eventStream.outFcmLiveCampaign().
                        send(MessageBuilder.withPayload(LiveCampaignTriggerInfo(campaign.id!!,clientId,user.id!!,templateId)).build())
//                val notification = fcmAndroidMessage(clientId, campaign, user, templateId)
//                toKafka(notification)
            }
            if (campaign.campaignType == "PUSH_WEB") {
                if (user.communication?.webpush == null || user.communication?.webpush?.dnd == true)
                    return //Local lambda return
                eventStream.outFcmLiveCampaign().
                        send(MessageBuilder.withPayload(LiveCampaignTriggerInfo(campaign.id!!,clientId,user.id!!,templateId)).build())
//                user.identity.webFcmToken?.forEach {
//                    val notification = fcmWebMessage(clientId, campaign, user, it, templateId)
//                    toKafka(notification)
//                }
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