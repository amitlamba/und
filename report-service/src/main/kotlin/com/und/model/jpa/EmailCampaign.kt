package com.und.model.jpa

import com.und.model.jpa.Campaign
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "email_campaign")
class EmailCampaign {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "email_campaign_id_seq")
    @SequenceGenerator(name = "email_campaign_id_seq", sequenceName = "email_campaign_id_seq", allocationSize = 1)
    var emailCampaignId: Long? = null

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

    @Column(name = "email_template_id")
    //@NotNull
    var templateId: Long? = null

    @field:CreationTimestamp
    @Column(name = "date_created", updatable = false)
    lateinit var dateCreated: LocalDateTime

    @field:UpdateTimestamp
    @Column(name = "date_modified")
    lateinit var dateModified: LocalDateTime

    @Column(name="client_setting_email_id")
    @NotNull
    var clientSettingEmailId:Long?=null
}