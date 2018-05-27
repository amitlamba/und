package com.und.model.jpa

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotNull

@Entity
@Table(name = "client_setting_email")
class ClientSettingsEmail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "client_setting_email_id_seq")
    @SequenceGenerator(name = "client_setting_email_id_seq", sequenceName = "client_setting_email_id_seq", allocationSize = 1)
    var id: Long? = null

    @NotNull
    var clientId: Long? = null

    @NotNull
    @Column(name = "address")
    var address: String? = null

    @NotNull
    var verified: Boolean? = null

    @NotNull
    @Email
    @Column(name = "email")
    var email: String? = null

    @NotNull
    var deleted: Boolean = false

    @Transient
    @field:CreationTimestamp
    @Column(name = "date_created")
    lateinit var dateCreated: LocalDateTime

    @field:UpdateTimestamp
    @Column(name = "date_modified")
    lateinit var dateModified: LocalDateTime

/*    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_setting_id")
    var clientSetting: ClientSettings? = null*/

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClientSettingsEmail

        if (clientId != other.clientId) return false
        if (email != other.email) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clientId?.hashCode() ?: 0
        result = 31 * result + (email?.hashCode() ?: 0)
        return result
    }


}


