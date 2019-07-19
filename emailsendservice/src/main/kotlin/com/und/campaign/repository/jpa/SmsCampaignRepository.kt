package com.und.campaign.repository.jpa

import com.und.model.jpa.SmsCampaign
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SmsCampaignRepository :JpaRepository<SmsCampaign,Long>{
    @Query("select * from sms_campaign where campaign_id= :id",nativeQuery = true)
    fun findByCampaignId(@Param("id")campaignId:Long): Optional<SmsCampaign>
}