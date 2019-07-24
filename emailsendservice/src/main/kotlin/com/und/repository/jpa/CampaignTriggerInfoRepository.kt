package com.und.repository.jpa

import com.und.campaign.model.CampaignTriggerInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CampaignTriggerInfoRepository:JpaRepository<CampaignTriggerInfo,Long> {
    @Query("update table campaignTriggerInfo set error = :status where campaign_id = :id",nativeQuery = true)
    fun updateErrorStatus(@Param("id")campaignId:Long, @Param("status")errorStatus:Boolean)
}