package com.und.model.jpa


import com.und.exception.EmailError
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "email_failure_audit_log")
class EmailFailureAuditLog {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "email_failure_audit_log_id_seq")
    @SequenceGenerator(name = "email_failure_audit_log_id_seq", sequenceName = "email_failure_audit_log_id_seq", allocationSize = 1)
    var id: Long? = null

    @Column(name = "client_id")
    @NotNull
    var clientID: Long? = null

    @Column(name = "client_setting_email_id")
    @NotNull
    var clientSettingId: Long? = 0

    @Column(name = "message")
    @NotNull
    var message: String? = ""


    @Column(name = "status", updatable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    lateinit var status: EmailError.FailureType

    @field:CreationTimestamp
    @Column(name = "date_created", updatable = false)
    lateinit var dateCreated: LocalDateTime


}
