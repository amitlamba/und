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
    var id: Long? = null

    @NotNull
    var clientId: Long? = null



    @Column(name = "email")
    var email: String? = null

    @Column(name = "service_provider_id", nullable = false)
    var serviceProviderId: Long? = null

}