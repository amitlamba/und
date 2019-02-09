package com.und.model.jpa

import javax.persistence.*

@Entity
@Table(name = "email_campaign")
class EmailCampaign {

    @Id
    @Column(name = "id")
    var emailCampaignId: Long? = null

    @Column(name = "client_id")
    var clientID: Long? = null

    @Column(name = "email_template_id")
    var templateId: Long? = null


    @Column(name="client_setting_email_id")
    var clientSettingEmailId:Long?=null

}