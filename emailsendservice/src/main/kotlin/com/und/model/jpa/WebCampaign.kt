package com.und.model.jpa

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "webpush_campaign_table")
class WebPushCampaign {
    @Id
    var id:Long?=null
    @Column(name="client_id")
    var clientId:Long?=null
    @Column(name="template_id")
    var templateId:Long?=null
}