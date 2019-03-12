package com.und.model.jpa

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotNull

//@Entity
//@Table(name = "campaign")
//class Campaign {
//
//    @Id
//    @Column(name = "id")
//    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "campaign_id_seq")
//    @SequenceGenerator(name = "campaign_id_seq", sequenceName = "campaign_id_seq", allocationSize = 1)
//    var campaignId: Long = 0
//
//    @Column(name = "segmentation_id")
//    var segmentId: Long = 0
//    @Column(name = "campaign_type")
//    lateinit var campaignType: String
//    @Column(name = "email_template_id")
//    var emailTemplateId: Long? = null
//    @Column(name = "email_template_name")
//    var emailTemplateName: String? = null
//    @Column(name = "sms_template_id")
//    var smsTemplateId: Long? = null
//    @Column(name = "email_from_user")
//    var fromEmailAddress: String? = null
//    @Column(name = "sms_from_user")
//    var fromSMSUser: String? = null
//    @Column(name = "client_id")
//    var clientId: Long = 0
//    @Column(name = "android_template_id")
//    var androidTemplateId: Long? = null
//    @Column(name = "web_template_id")
//    var webTemplateId: Long? = null
//    @Column(name = "service_provider_id")
//    var serviceProviderId: Long? = null
////    @Column(name = "ios_template_id")
////    var iosTemplateId:Long?=null
//}

@Entity
@Table(name = "campaign")
class Campaign {

    @Id
    @Column(name = "id")
    var id: Long? = null

    @Column(name = "client_id")
    var clientID: Long? = null

    @Column(name = "campaign_type") //Email / SMS / Notifications etc
    lateinit var campaignType: String

    @Column(name = "segmentation_id") //TODO Foreign Key
    var segmentationID: Long?=null

    @Column(name = "conversion_event")
    var conversionEvent: String? = null

    @Column(name = "service_provider_id")
    var serviceProviderId: Long? = null
    //TODO add sms, and push campaign later

    @Column(name = "from_user")
    var fromUser: String? = null

    @Column(name="start_date")
    var startDate:LocalDateTime?=null

    @Column(name="end_date")
    var endDate:LocalDateTime?=null
}