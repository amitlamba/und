package com.und.repository.jpa

import com.und.model.jpa.Campaign
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CampaignRepository : JpaRepository<Campaign, Long> {

    @Query("""select
                      c.id,
                      c.segmentation_id,
                      c.campaign_type,
                      ec.email_template_id,
                      et.name as email_template_name,
                      sc.sms_template_id,
                      et.from_user as email_from_user,
                      st.from_user as sms_from_user,
                      c.client_id
                    from campaign c
                      LEFT JOIN email_campaign ec on c.id = ec.campaign_id and ec.client_id = c.client_id
                      LEFT JOIN sms_campaign sc on c.id = sc.campaign_id and sc.client_id = c.client_id
                      LEFT JOIN email_template et on et.id = ec.email_template_id and et.client_id = c.client_id
                      LEFT JOIN sms_template st on st.id = sc.sms_template_id and st.client_id = c.client_id
                    where c.id = :campaignId and (c.campaign_status <> 'deleted' or c.campaign_status is null) and c.client_id = :clientId""",
            nativeQuery = true)
    fun getCampaignByCampaignId(campaignId: Long, clientId: Long): Campaign?
}