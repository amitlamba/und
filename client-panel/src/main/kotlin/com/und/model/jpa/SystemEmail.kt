package com.und.model.jpa

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name="system_email")
class SystemEmail {

    @Id
    var id:Long?=null

    @Column(name="name")
    var name:String?=null
    @Column(name="email_setting_id")
    var emailSettingId:Long?=null
    @Column(name="email_template_id")
    var emailTemplateId:Long?=null
}