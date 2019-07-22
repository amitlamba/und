package com.und.campaign.listener

import com.und.campaign.repository.redis.LiveSegmentCampaignRepository
import com.und.campaign.service.CampaignService
import com.und.campaign.service.TestCampaignService
import com.und.exception.EventUserNotFoundException
import com.und.model.jpa.Campaign
import com.und.model.jpa.CampaignStatus
import com.und.model.jpa.TypeOfCampaign
import com.und.model.livesegment.LiveSegmentUser
import com.und.model.mongo.EventUser
import com.und.model.redis.LiveSegmentCampaign
import com.und.model.redis.LiveSegmentCampaignCache
import com.und.model.utils.TestCampaign
import com.und.utils.loggerFor
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CampaignListener {

    @Autowired
    private lateinit var campaignService: CampaignService

    @Autowired
    private lateinit var testCampaignService: TestCampaignService

//    @Autowired
//    private lateinit var eventUserRepository: EventUserRepository
//
//    @Autowired
//    private lateinit var liveSegmentTrackRepository: LiveSegmentTrackRepository

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

//    @Autowired
//    private lateinit var eventStream: EventStream
//
//    @Autowired
//    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var liveSegmentCampaignRepository: LiveSegmentCampaignRepository

    companion object {
        val logger: Logger = loggerFor(CampaignListener::class.java)
    }


    @StreamListener(value = "campaignTriggerReceive")
    fun executeCampaign(campaignData: Pair<Long, Long>) {
        try {
            val (campaignId, clientId) = campaignData
            logger.debug("campaign trigger with id $campaignId and $clientId")
            campaignService.executeCampaign(campaignId, clientId)
        } catch (ex: Exception) {
            //TODO update status to error
            logger.error("error occurred", ex)
        } finally {
            logger.info("complete")
        }
    }

    @StreamListener("receiveManualTriggerCampaign")
    fun runManualCampaign(data: Pair<Long, Long>) {
        try {
            val (campaignId, clientId) = data
            logger.debug("Manual campaign trigger with id $campaignId and $clientId")
            campaignService.executeCampaignForAbManual(campaignId, clientId)
        } catch (ex: Exception) {
            //TODO update status to error
            logger.error("error occurred", ex)
        } finally {
            logger.info("complete")
        }
    }

    @StreamListener("abCampaignTriggerReceive")
    fun executeAbCampaign(campaignData: Pair<Long, Long>) {
        try {
            val (campaignId, clientId) = campaignData
            logger.debug("ab campaign trigger with id $campaignId and $clientId")
            campaignService.executeCampaignForAb(campaignId, clientId)
        } catch (ex: Exception) {
            //TODO update status to error
            logger.error("error occurred", ex)
        } finally {
            logger.info("complete")
        }
    }

    @StreamListener("inTestCampaign")
    fun executeTestCampaign(testCampaign: TestCampaign) {
        logger.debug("Test campaign triggered.")
        try {
            testCampaignService.executeTestCampaign(testCampaign)
            logger.debug("Test campaign complete.")
        }catch (ex:Exception){
            logger.debug("Error occurred in test campaign.")
        }

    }

    @StreamListener(value = "inLiveSegment")
    fun executeLiveSegmentCampaign(liveSegmentUser: LiveSegmentUser) {
        try {
            val segmentId = liveSegmentUser.segmentId
            val liveSegmentId = liveSegmentUser.liveSegmentId
            val clientId = liveSegmentUser.clientId
            val userId = liveSegmentUser.userId


            val user = mongoTemplate.find(Query().addCriteria(Criteria.where("_id").`is`(ObjectId(userId))), EventUser::class.java, "${clientId}_eventUser")
            if (user.isEmpty()) throw EventUserNotFoundException("User Not Found.")

            val campaignList = /*campaignService.findAllLiveSegmentCampaignBySegmentId(segmentId, clientId)*/ getCampaigns(clientId, segmentId)
            val filteredCampaigns = mutableListOf<Campaign>()
            if (campaignList.isEmpty())
                campaignService.updateCampaignStatus(CampaignStatus.COMPLETED, clientId, segmentId)
            else {
                campaignList.forEach {

                    if (it.endDate!!.isBefore(LocalDateTime.now())) {
                        var liveCampaigns=getCampaignList(clientId, segmentId)
                        val campaignId = it.id
                        liveCampaigns = liveCampaigns.filter { it.campaignId!=campaignId }
                        updateLiveSegmentCampaignsListR(clientId,it.segmentationID!!,liveCampaigns)
                        campaignService.updateCampaignStatusByCampaignId(CampaignStatus.COMPLETED, clientId, it.id!!)
                    } else {
                        filteredCampaigns.add(it)
                    }
                }
            }
            filteredCampaigns.forEach { campaign ->
                logger.debug("campaign live trigger with id $segmentId and $clientId and $userId and campaign id ${campaign.id}")
                when (campaign.typeOfCampaign) {
                    TypeOfCampaign.AB_TEST -> {
                        campaignService.newExecuteAbTestLiveCampaign(campaign, clientId, user[0])
                    }
                    TypeOfCampaign.SPLIT -> {
                        campaignService.newExecuteSplitLiveCampaign(campaign, clientId, user[0])
                    }
                    else -> {
                        campaignService.executeLiveCampaign(campaign, clientId, user[0])
                    }
                }

            }
        } catch (ex: Exception) {
            logger.error("error occurred", ex)
        } finally {
            logger.info("complete")
        }
    }

    fun getCampaigns(clientId: Long, segmentId: Long): List<Campaign> {
        val liveCampaigns=getCampaignListR(clientId, segmentId)
        val campaignIds = mutableListOf<Long>()
        liveCampaigns.forEach {
            val campaignId= when {
                "CREATED"==it.status && it.startDate!!.compareTo(LocalDateTime.now()) < 0 -> it.campaignId
                else -> {-1}
            }
            if(campaignId != -1L) campaignIds.add(campaignId)

        }
        val campaigns = mutableListOf<Campaign>()
        campaignIds.forEach {
           val campaign = getCampaign(clientId,it)
            if(campaign!=null) campaigns.add(campaign)
        }
        return campaigns
    }

    @Cacheable(value = ["liveSegmentCampaigns"], key = "'clientId_'+#clientId+'segmentId_'+#segmentId")
    fun getCampaignList(clientId: Long, segmentId: Long): List<LiveSegmentCampaign> {
        return emptyList()
    }

    @CachePut(value = ["liveSegmentCampaigns"], key = "'clientId_'+#clientId+'segmentId_'+#segmentId")
    fun updateLiveSegmentCampaignList(clientId: Long,segmentId: Long,list: List<LiveSegmentCampaign>):List<LiveSegmentCampaign>{
        return list
    }

    fun updateLiveSegmentCampaignsListR(clientId: Long, segmentId: Long, liveSegmentCampaign: List<LiveSegmentCampaign>) {
        var liveSegmentCampaignCache = LiveSegmentCampaignCache()
        liveSegmentCampaignCache.id = "clientId_${clientId}:segmentId_${segmentId}"
        liveSegmentCampaignCache.liveSegmentCampaign = liveSegmentCampaign
        liveSegmentCampaignRepository.save(liveSegmentCampaignCache)
    }

    fun getCampaignListR(clientId: Long, segmentId: Long): List<LiveSegmentCampaign> {
        val id = "clientId_${clientId}:segmentId_${segmentId}"
        val cacheResult=liveSegmentCampaignRepository.findById(id)
        return if (cacheResult.isPresent) cacheResult.get().liveSegmentCampaign else emptyList()
    }


    @Cacheable(value = ["campaigns"],key = "'clientId'+#clientId+'campaignId'+#campaignId")
    fun getCampaign(clientId: Long,campaignId:Long):Campaign?{
        return campaignService.findCampaignById(campaignId)
    }

}