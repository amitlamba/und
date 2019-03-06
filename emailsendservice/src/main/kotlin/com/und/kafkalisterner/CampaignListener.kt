package com.und.kafkalisterner

import com.und.exception.EventUserNotFoundException
import com.und.model.livesegment.LiveSegmentUser
import com.und.model.mongo.LiveSegmentTrack
import com.und.repository.mongo.EventUserRepository
import com.und.repository.mongo.LiveSegmentTrackRepository
import com.und.service.CampaignService
import com.und.utils.loggerFor
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.stereotype.Service

@Service
class CampaignListener {

    @Autowired
    private lateinit var campaignService: CampaignService

    @Autowired
    private lateinit var eventUserRepository: EventUserRepository

    @Autowired
    private lateinit var liveSegmentTrackRepository: LiveSegmentTrackRepository

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
            val user = eventUserRepository.findById(userId).orElseThrow { EventUserNotFoundException("User Not Found") }
            val campaignList = campaignService.findLiveSegmentCampaign(segmentId, clientId)
            logger.debug("campaign live trigger with id $segmentId and $clientId and $userId")
            campaignList.forEach { campaign ->
                logger.debug("campaign live trigger with id $segmentId and $clientId and $userId and campaign id $campaign.id")
                campaignService.executeLiveCampaign(campaign, clientId, user)
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