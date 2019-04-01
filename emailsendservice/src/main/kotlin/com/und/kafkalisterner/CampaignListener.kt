package com.und.kafkalisterner

import com.und.exception.EventUserNotFoundException
import com.und.model.jpa.Campaign
import com.und.model.jpa.CampaignStatus
import com.und.model.jpa.TypeOfCampaign
import com.und.model.livesegment.LiveSegmentUser
import com.und.model.mongo.EventUser
import com.und.model.mongo.LiveSegmentTrack
import com.und.model.utils.TestCampaign
import com.und.repository.mongo.EventUserRepository
import com.und.repository.mongo.LiveSegmentTrackRepository
import com.und.service.CampaignService
import com.und.service.TestCampaignService
import com.und.utils.loggerFor
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
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

    @Autowired
    private lateinit var eventUserRepository: EventUserRepository

    @Autowired
    private lateinit var liveSegmentTrackRepository: LiveSegmentTrackRepository

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

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
            logger.error("error occurred", ex)
        } finally {
            logger.info("complete")
        }
    }

    @StreamListener("abCampaignTriggerReceive")
    fun executeAbCampaign(campaignData: Pair<Long, Long>){
        try {
            val (campaignId, clientId) = campaignData
            logger.debug("campaign trigger with id $campaignId and $clientId")
            campaignService.executeCampaignForAb(campaignId, clientId)
        } catch (ex: Exception) {
            logger.error("error occurred", ex)
        } finally {
            logger.info("complete")
        }
    }

    @StreamListener("inTestCampaign")
    fun executeTestCampaign(testCampaign: TestCampaign){
        testCampaignService.executeTestCampaign(testCampaign)
    }

    @StreamListener(value = "inLiveSegment")
    fun executeLiveSegmentCampaign(liveSegmentUser: LiveSegmentUser) {
        try {
            val segmentId = liveSegmentUser.segmentId
            val liveSegmentId = liveSegmentUser.liveSegmentId
            val clientId = liveSegmentUser.clientId
            val userId = liveSegmentUser.userId


            trackSegmentUser(clientId, liveSegmentId, segmentId, userId)
            /***FIXED findById return empty but user present for this userId
             *case 1 Id is String type in our repository but in real case its ObjectId in mongo.I try it but no success.
             *case 2 I think spring resolve it to id field as we see in jpa  but in mongo its _id This may be reason but not sure.
            **/
//            val user = eventUserRepository.findById(userId).orElseThrow { EventUserNotFoundException("User Not Found") }
            val user=mongoTemplate.find(Query().addCriteria(Criteria.where("_id").`is`(ObjectId(userId))),EventUser::class.java,"${clientId}_eventUser")
            if(user.isEmpty()) throw EventUserNotFoundException("User Not Found.")
            //get all campaigns associated with live segmentid
//            val campaignList = campaignService.findLiveSegmentCampaign(segmentId, clientId)
            //refresh cache I m thinking aboout schedulae ajob which update the status of live campaign
            //a stop cam newer start again
            val campaignList=campaignService.findAllLiveSegmentCampaignBySegmentId(segmentId, clientId)
            //FIXME if campaign list is empty then update the status of campaign to completed for this segment id.

            //we imporve it find all cmapign with this segmentid if there endTime is passed mark it completed here if
            //we have two campaign with this id then and one is completed then we are not updating it.
            val filteredCampaigns= mutableListOf<Campaign>()
            if(campaignList.isEmpty())
            campaignService.updateCampaignStatus(CampaignStatus.COMPLETED,clientId,segmentId)
            else{
                campaignList.forEach {
                    if(it.endDate!!.isBefore(LocalDateTime.now())){
                        //remove that campaign from cache
                        campaignService.updateCampaignStatusByCampaignId(CampaignStatus.COMPLETED,clientId,it.id!!)
                    }else{
                        filteredCampaigns.add(it)
                    }
                }
            }
            logger.debug("campaign live trigger with id $segmentId and $clientId and $userId")
            filteredCampaigns.forEach { campaign ->
                logger.debug("campaign live trigger with id $segmentId and $clientId and $userId and campaign id $campaign.id")
                when(campaign.typeOfCampaign){
                    TypeOfCampaign.AB_TEST ->{
                        campaignService.executeAbTestLiveCampaign(campaign,clientId,user[0])
                    }
                    TypeOfCampaign.SPLIT -> {
                        campaignService.executeSplitLiveCampaign(campaign,clientId,user[0])
                    }
                    else ->{
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

    private fun trackSegmentUser(clientId: Long, liveSegmentId: Long, segmentId: Long, userId: String) {
        val liveSegmentTrack = LiveSegmentTrack(
                clientID = clientId,
                liveSegmentId = liveSegmentId,
                segmentId = segmentId,
                userId = userId
        )
        //TODO write it in dao layer.
        mongoTemplate.save(liveSegmentTrack,"${clientId}_livesegmenttrack")
        /**
         * in below code collection name is not resolve because on system call #{tenantProvider.getTenant()} not available.
         * here collection name is _livesegmenttrack
         */
//        val v=liveSegmentTrackRepository.save(liveSegmentTrack)
    }


}