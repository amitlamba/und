package com.und.repository.jpa

import com.und.model.jpa.WebPushCampaign
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface WebPushCampaignRepository:JpaRepository<WebPushCampaign,Long> {

    @Query("select * from webpush_campaign_table where campaign_id= :id",nativeQuery = true)
    fun findByCampaignId(@Param("id")campaignId:Long): Optional<WebPushCampaign>
}