package com.und.model.jpa

import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "fcm_failure_audit_log")
class FcmFailureAuditLog {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "fcm_failure_audit_log_id_seq")
    @SequenceGenerator(name = "fcm_failure_audit_log_id_seq", sequenceName = "fcm_failure_audit_log_id_seq", allocationSize = 1)
    var id: Long? = null

    @Column(name = "client_id")
    @NotNull
    var clientID: Long? = null

    @Column(name = "message")
    @NotNull
    var message: String? = ""

    @Column(name = "status")
    @NotNull
    var status: String? = ""

    @Column(name="error_code")
    var errorCode:Long?=null

    @Column(name="type")
    var type:String? = ""

    @field:CreationTimestamp
    @Column(name = "date_created", updatable = false)
    lateinit var dateCreated: LocalDateTime

}
