package com.und.repository.jpa

import com.und.model.jpa.Campaign
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Repository
interface CampaignRepository : JpaRepository<Campaign, Long>, PagingAndSortingRepository<Campaign, Long> {
//    fun findByClientID(clientID: Long): List<Campaign>
//    //fun findByClientIDAndCampaignStatus(clientID: Long = 1, status:CampaignStatus): List<Campaign>
    fun findByIdAndClientID(id: Long, clientID: Long): Optional<Campaign>
//
//    fun findByClientIDAndCampaignType(clientID: Long, campaignType: CampaignType): List<Campaign>
//    fun findByIdAndClientIDAndCampaignType(id: Long, clientID: Long, campaignType: CampaignType): Campaign
//
//    /*
//    * Before this annotation there is transaction required exception . But my record are saved in database.
//    * I think transaction annotation on saveCampaign method not working.
//    * */
//    @Transactional
//    @Modifying(clearAutomatically = true)
//    @Query("UPDATE campaign  SET campaign_status = :status, date_modified = now() WHERE client_id = :clientId and id=:campaignId", nativeQuery = true)
//    fun updateScheduleStatus(@Param("campaignId") campaignId: Long,
//                             @Param("clientId") clientId: Long,
//                             @Param("status") status: String)
//
//    @Modifying(clearAutomatically = true)
//    @Query("UPDATE campaign  SET schedule = :schedule, date_modified = now() WHERE client_id = :clientId and id=:campaignId", nativeQuery = true)
//    fun updateSchedule(@Param("campaignId") campaignId: Long,
//                       @Param("clientId") clientId: Long,
//                       @Param("schedule") schedule: String)
//
//    @Query("SELECT campaign_status from campaign c  WHERE c.client_id = :clientId and c.id=:campaignId", nativeQuery = true)
//    fun retrievecheduleStatus(@Param("campaignId") campaignId: Long,
//                              @Param("clientId") clientId: Long): String
//
//    @Query("SELECT id from Campaign c  WHERE c.clientID = :clientId and c.status in (:statuses)", nativeQuery = false)
//    fun findByStatusIn(clientId: Long, statuses: List<CampaignStatus>): List<Long>
//
    fun findByClientIDAndSegmentationID(clientId:Long, segmetationId:Long):List<Campaign>
}