package com.und.kafkalisterner

import com.und.exception.EventUserNotFoundException
import com.und.model.livesegment.LiveSegmentUser
import com.und.model.mongo.EventUser
import com.und.model.mongo.LiveSegmentTrack
import com.und.repository.mongo.EventUserRepository
import com.und.repository.mongo.LiveSegmentTrackRepository
import com.und.service.CampaignService
import com.und.utils.loggerFor
import org.bson.types.ObjectId
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Service

@Service
class CampaignListener {

    @Autowired
    private lateinit var campaignService: CampaignService

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


    @StreamListener(value = "inLiveSegment")
    fun executeLiveSegmentCampaign(liveSegmentUser: LiveSegmentUser) {
        try {
            val segmentId = liveSegmentUser.segmentId
            val liveSegmentId = liveSegmentUser.liveSegmentId
            val clientId = liveSegmentUser.clientId
            val userId = liveSegmentUser.userId


            trackSegmentUser(clientId, liveSegmentId, segmentId, userId)
            //get all campaigns associated with live segmentid
            //TODO there is error
//            val user = eventUserRepository.findById(userId).orElseThrow { EventUserNotFoundException("User Not Found") }
            val user=mongoTemplate.find(Query().addCriteria(Criteria.where("_id").`is`(ObjectId(userId))),EventUser::class.java,"${clientId}_eventUser")
            if(user.isEmpty()) throw EventUserNotFoundException("User Not Found.")
            val campaignList = campaignService.findLiveSegmentCampaign(segmentId, clientId)
            //FIXME if campaign list is empty then update the status of campaign to completed.else make it running.
            logger.debug("campaign live trigger with id $segmentId and $clientId and $userId")
            campaignList.forEach { campaign ->
                logger.debug("campaign live trigger with id $segmentId and $clientId and $userId and campaign id $campaign.id")
                campaignService.executeLiveCampaign(campaign, clientId, user[0])
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
        liveSegmentTrackRepository.save(liveSegmentTrack)
    }


}