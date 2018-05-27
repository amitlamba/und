package com.und.model.jpa

import com.und.model.MessageType
import com.und.model.Status
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Null

@Entity
@Table(name = "sms_template")
class SmsTemplate {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "sms_template_id_seq")
    @SequenceGenerator(name = "sms_template_id_seq", sequenceName = "sms_template_id_seq", allocationSize = 1)
    var id: Long? = null

    @Column(name = "client_id")
    @NotNull
    var clientID: Long? = null

    @Column(name = "appuser_id")
    @NotNull
    var appuserID: Long? = null

    @Column(name = "name")
    @NotNull
    lateinit var name: String

    @Column(name = "sms_template_body")
    @NotNull
    lateinit var smsTemplateBody: String

    @Column(name = "parent_id")
    @Null
    var parentID: Long? = null

    @Column(name = "from_user")
    @NotNull
    lateinit var from: String

    @Column(name = "message_type") //Promotional or Transactional
    @NotNull
    @Enumerated(EnumType.STRING)
    var messageType: MessageType? = null

    @Column(name = "tags")
    var tags: String? = null

    @Column(name = "status")
    @NotNull
    @Enumerated(EnumType.STRING)
    lateinit var status: Status

    @field:CreationTimestamp
    @Column(name = "date_created")
    lateinit var dateCreated: LocalDateTime

    @field:UpdateTimestamp
    @Column(name = "date_modified")
    lateinit var dateModified: LocalDateTime
}

