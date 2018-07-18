package com.und.model.jpa


import com.und.model.JobActionStatus
import com.und.model.JobDescriptor
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "campaign_audit_log")
class CampaignAuditLog {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "campaign_audit_log_id_seq")
    @SequenceGenerator(name = "campaign_audit_log_id_seq", sequenceName = "campaign_audit_log_id_seq", allocationSize = 1)
    var id: Long? = null

    @Column(name = "client_id")
    @NotNull
    var clientID: Long? = null

    @Column(name = "campaign_id")
    @NotNull
    var campaignId: Long = 0

    @Column(name = "message")
    @NotNull
    var message: String = ""


    @Column(name = "status", updatable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    lateinit var status:JobActionStatus.Status

    @Column(name = "action", updatable = false)
    @NotNull
    @Enumerated(EnumType.STRING)
    lateinit var action: JobDescriptor.Action


    @field:CreationTimestamp
    @Column(name = "date_created", updatable = false)
    lateinit var dateCreated: LocalDateTime


}
