package com.und.model.jpa

import com.und.model.jpa.Campaign
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "sms_campaign")
class SmsCampaign {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "sms_campaign_id_seq")
    @SequenceGenerator(name = "sms_campaign_id_seq", sequenceName = "sms_campaign_id_seq", allocationSize = 1)
    var smsCampaignId: Long? = null

    @Column(name = "client_id")
    @NotNull
    var clientID: Long? = null

    @Column(name = "appuser_id")
    @NotNull
    var appuserId: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id")
    @NotNull
    lateinit var campaign: Campaign

    @Column(name = "sms_template_id")
    @NotNull
    var templateId: Long? = null

    @field:CreationTimestamp
    @Column(name = "date_created")
    lateinit var dateCreated: LocalDateTime

    @field:UpdateTimestamp
    @Column(name = "date_modified")
    lateinit var dateModified: LocalDateTime

}