package com.und.kafkalisterner

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.config.EventStream
import com.und.exception.EventUserNotFoundException
import com.und.model.jpa.*
import com.und.model.livesegment.LiveSegmentUser
import com.und.model.mongo.EventUser
import com.und.model.mongo.LiveSegmentTrack
import com.und.model.redis.LiveSegmentCampaign
import com.und.model.utils.JobDescriptor
import com.und.model.utils.TestCampaign
import com.und.repository.mongo.EventUserRepository
import com.und.repository.mongo.LiveSegmentTrackRepository
import com.und.service.CampaignService
import com.und.service.TestCampaignService
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
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class CampaignListener {

    @Autowired
    private lateinit var campaignService: CampaignService

    @Autowired
    private lateinit var testCampaignService: TestCampaignService

    @Autowired
    private lateinit var eventUserRepository: EventUserRepository

    @Autowired
    private lateinit var liveSegmentTrackRepository: LiveSegmentTrackRepository

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var eventStream: EventStream

    @Autowired
    private lateinit var objectMapper: ObjectMapper

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
            logger.debug("campaign trigger with id $campaignId and $clientId")
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
        testCampaignService.executeTestCampaign(testCampaign)
    }

    @StreamListener(value = "inLiveSegment")
    fun executeLiveSegmentCampaign(liveSegmentUser: LiveSegmentUser) {
        try {
            val segmentId = liveSegmentUser.segmentId
            val liveSegmentId = liveSegmentUser.liveSegmentId
            val clientId = liveSegmentUser.clientId
            val userId = liveSegmentUser.userId


//            trackSegmentUser(clientId, liveSegmentId, segmentId, userId)
            /***FIXED findById return empty but user present for this userId
             *case 1 Id is String type in our repository but in real case its ObjectId in mongo.I try it but no success.
             *case 2 I think spring resolve it to id field as we see in jpa  but in mongo its _id This may be reason but not sure.
             **/
//            val user = eventUserRepository.findById(userId).orElseThrow { EventUserNotFoundException("User Not Found") }
            val user = mongoTemplate.find(Query().addCriteria(Criteria.where("_id").`is`(ObjectId(userId))), EventUser::class.java, "${clientId}_eventUser")
            if (user.isEmpty()) throw EventUserNotFoundException("User Not Found.")
            //get all campaigns associated with live segmentid
//            val campaignList = campaignService.findLiveSegmentCampaign(segmentId, clientId)
            //refresh cache I m thinking aboout schedulae ajob which update the status of live campaign
            //a stop cam newer start again

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
                        updateLiveSegmentCampaignList(clientId,it.segmentationID!!,liveCampaigns)
                        campaignService.updateCampaignStatusByCampaignId(CampaignStatus.COMPLETED, clientId, it.id!!)
                    } else {
                        filteredCampaigns.add(it)
                    }
                }
            }
            logger.debug("campaign live trigger with id $segmentId and $clientId and $userId")
            filteredCampaigns.forEach { campaign ->
                logger.debug("campaign live trigger with id $segmentId and $clientId and $userId and campaign id $campaign.id")
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
        val liveCampaigns=getCampaignList(clientId, segmentId)
        val campaignIds = emptyList<Long>()
        liveCampaigns.forEach {
            when(it.status){
                "CREATED" -> it.campaignId
            }
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
    @Cacheable(value = ["campaigns"],key = "'client'+#clientId+'campaign'+#campaignId")
    fun getCampaign(clientId: Long,campaignId:Long):Campaign?{
        return campaignService.findCampaignById(campaignId)
    }

}