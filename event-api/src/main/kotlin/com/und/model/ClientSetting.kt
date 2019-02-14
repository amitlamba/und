package com.und.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name="client_settings")
class ClientSettings {

    @Id
    var id: Long? = null

    @Column(name = "client_id")
    var clientID: Long? = null

    @Column(name = "timezone")
    var timezone: String = "UTC"
}