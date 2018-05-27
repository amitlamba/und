package com.und.model.jpa

import com.und.model.jpa.security.Client
import org.hibernate.annotations.DynamicUpdate
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "CLIENT_VERIFICATION")
@DynamicUpdate(true)
class ClientVerification {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "client_verification_id_seq")
    @SequenceGenerator(name = "client_verification_id_seq", sequenceName = "client_verification_id_seq", allocationSize = 1)
    var id: Long? = null

    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="CLIENT_ID")
    lateinit var client: Client

    @Column(name = "email_code", length = 255, unique = true)
    @NotNull
    lateinit var emailCode: String

    @Column(name = "phone_otp", length = 255)
    lateinit var phoneOtp: String

    @Column(name = "email_code_date")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    lateinit var emailCodeDate: Date

    @Column(name = "phone_otp_date")
    @Temporal(TemporalType.TIMESTAMP)
    lateinit var phoneOtpDate: Date

}