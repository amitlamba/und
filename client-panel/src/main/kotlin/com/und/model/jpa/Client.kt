package com.und.model.jpa

import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "CLIENT")
class Client {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "client_id_seq")
    @SequenceGenerator(name = "client_id_seq", sequenceName = "client_id_seq", allocationSize = 1)
    var id: Long? = null

    //TODO CREATE STATE COLUMN

    @Column(name = "NAME", length = 255)
    @NotNull
    lateinit var name: String

    @Column(name = "EMAIL", length = 255, unique = true)
    @NotNull
    lateinit var email: String

    @Column(name = "PHONE", length = 50)
    var phone: String? = null

    @Column(name = "email_verified")
    @NotNull
    var emailVerified: Boolean = false

    @Column(name = "phone_verified")
    @NotNull
    var phoneVerified: Boolean = false

    @Column(name = "address")
    var address: String? = null

    @Column(name = "firstname")
    var firstname: String? = null

    @Column(name = "lastname")
    var lastname: String? = null

    @Column(name = "country")
    var country: String? = null

    @Column(name = "date_created")
    @Temporal(TemporalType.TIMESTAMP)
    lateinit var dateCreated: Date

    @Column(name = "date_modified")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    lateinit var dateModified: Date
}