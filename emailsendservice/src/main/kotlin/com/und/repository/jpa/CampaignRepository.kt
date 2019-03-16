package com.und.repository.jpa

import com.und.model.jpa.Campaign
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Repository
interface CampaignRepository : JpaRepository<Campaign, Long> {
    //here we select st.name as sms_template_name
//    LEFT JOIN notification_template_android nta on nta.id = ac.template_id and nta.client_id = c.client_id
    @Query("""select
                      c.id,
                      c.segmentation_id,
                      c.campaign_type,
                      c.campaign_status,
                      c.service_provider_id,
                      ec.email_template_id,
                      et.name as email_template_name,
                      sc.sms_template_id,
                      et.from_user as email_from_user,
                      st.from_user as sms_from_user,
                      c.client_id,
                      c.start_date,
                      c.end_date,
                      ac.template_id as android_template_id,
                      wc.template_id as web_template_id
                    from campaign c
                      LEFT JOIN email_campaign ec on c.id = ec.campaign_id and ec.client_id = c.client_id
                      LEFT JOIN sms_campaign sc on c.id = sc.campaign_id and sc.client_id = c.client_id
                      LEFT JOIN android_campaign ac on c.id = ac.campaign_id and ac.client_id = c.client_id
                      LEFT JOIN webpush_campaign_table wc on c.id = wc.campaign_id and wc.client_id = c.client_id
                      LEFT JOIN email_template et on et.id = ec.email_template_id and et.client_id = c.client_id
                      LEFT JOIN sms_template st on st.id = sc.sms_template_id and st.client_id = c.client_id

                    where c.id = :campaignId and (c.campaign_status <> 'deleted' or c.campaign_status is null) and c.client_id = :clientId""",
            nativeQuery = true)
    fun getCampaignByCampaignId(campaignId: Long, clientId: Long): Optional<Campaign>

    @Query("""select * from campaign c where c.client_id= :clientId and c.segmentation_id= :segmentId and c.campaign_status= 'CREATED' and c.end_date > current_timestamp """,nativeQuery = true)
    fun getCampaignByClientIDAndSegmentationIDAndEndDateAfter(@Param("segmentId")segmentId: Long, @Param("clientId")clientId: Long): List<Campaign>

    @Transactional
    @Modifying
    @Query("""update campaign set campaign_status = :status where segmentation_id = :segmentId and client_id = :clientId""",nativeQuery = true)
    fun updateStatusOfCampaign(@Param("status")status:String,@Param("segmentId")segmentId: Long,@Param("clientId")clientId:Long)
}