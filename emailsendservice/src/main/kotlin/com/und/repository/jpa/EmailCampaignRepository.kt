package com.und.repository.jpa

import com.und.model.jpa.EmailCampaign
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface EmailCampaignRepository:JpaRepository<EmailCampaign,Long> {

    @Query("select * from email_campaign where campaign_id = :id",nativeQuery = true)
    fun findByCampaignId(@Param("id")campaignId:Long): Optional<EmailCampaign>
}