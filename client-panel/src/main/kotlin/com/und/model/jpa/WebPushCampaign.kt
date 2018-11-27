package com.und.model.jpa

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "webpush_campaign_table")
class WebPushCampaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY,generator = "webpush_campaign_id_seq")
    @SequenceGenerator(name="webpush_campaign_id_seq",sequenceName = "webpush_campaign_table_id_seq",allocationSize = 1)
    var id:Long?=null
    @NotNull
    @Column(name="client_id")
    var clientId:Long?=null
    @NotNull
    @Column(name="appuser_id")
    var appuserId:Long?=null
    @NotNull
    @OneToOne
    @JoinColumn(name="campaign_id")
    lateinit var campaign:Campaign
    @NotNull
    @Column(name="template_id")
    var templateId:Long?=null
    @field:CreationTimestamp
    @Column(name="creation_date",updatable = false)
    lateinit var creationTime: LocalDateTime
    @field:UpdateTimestamp
    @Column(name="date_modified")
    lateinit var dataModified: LocalDateTime
}