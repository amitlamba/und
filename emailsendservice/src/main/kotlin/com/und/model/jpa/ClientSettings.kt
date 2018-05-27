package com.und.model.jpa

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "client_settings")
class ClientSettings {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "client_settings_id_seq")
    @SequenceGenerator(name = "client_settings_id_seq", sequenceName = "client_settings_id_seq", allocationSize = 1)
    var id: Long? = null

    @Column(name = "client_id")
    @NotNull
    var clientID: Long? = null

    @Transient
    @Column(name = "sender_email_addresses")
    var senderEmailAddresses: String? = null

    @Column(name = "authorized_urls")
    var authorizedUrls: String? = null

    @Column(name = "timezone")
    var timezone: String = "UTC"

    @Transient
    @field:CreationTimestamp
    @Column(name = "date_created")
    lateinit var dateCreated: LocalDateTime

    @field:UpdateTimestamp
    @Column(name = "date_modified")
    lateinit var dateModified: LocalDateTime

    @Column(name = "unsubscribe_link")
    var unSubscribeLink: String? = null

}