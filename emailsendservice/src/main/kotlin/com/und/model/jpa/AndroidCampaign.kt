package com.und.model.jpa

import javax.persistence.*


@Entity
@Table(name="android_campaign")
class AndroidCampaign {
    @Id
    var id:Long?=null
    @Column(name="client_id")
    var clientId:Long?=null
    @Column(name="template_id")
    var templateId:Long?=null
}