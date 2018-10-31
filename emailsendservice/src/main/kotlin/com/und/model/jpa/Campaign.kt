package com.und.model.jpa

import javax.persistence.*

@Entity
@Table(name = "campaign")
class Campaign {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "campaign_id_seq")
    @SequenceGenerator(name = "campaign_id_seq", sequenceName = "campaign_id_seq", allocationSize = 1)
    var campaignId: Long = 0
    @Column(name = "segmentation_id")
    var segmentId: Long = 0
    @Column(name = "campaign_type")
    lateinit var campaignType: String
    @Column(name = "email_template_id")
    var emailTemplateId: Long? = null
    @Column(name = "email_template_name")
    var emailTemplateName: String? = null
    @Column(name = "sms_template_id")
    var smsTemplateId: Long? = null
    @Column(name = "email_from_user")
    var fromEmailAddress: String? = null
    @Column(name = "sms_from_user")
    var fromSMSUser: String? = null
    @Column(name = "client_id")
    var clientId: Long = 0
    @Column(name="android_template_id")
    var androidTemplateId:Long?=null
}