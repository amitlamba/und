package com.und.model.jpa

import javax.persistence.*

@Entity
@Table(name = "campaign")
class Campaign {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "campaign_id_seq")
    @SequenceGenerator(name = "campaign_id_seq", sequenceName = "campaign_id_seq", allocationSize = 1)
    var campaignId: Long? = null
    @Column(name = "segmentation_id")
    var segmentId: Long? = null
    @Column(name = "campaign_type")
    var campaignType: String? = null
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
    var clientId: Long? = null
}