package com.und.model.jpa

import javax.persistence.*

@Entity
@Table(name = "sms_campaign")
class SmsCampaign {

    @Id
    @Column(name = "id")
    var smsCampaignId: Long? = null

    @Column(name = "client_id")
    var clientID: Long? = null

    @Column(name = "sms_template_id")
    var templateId: Long? = null


}