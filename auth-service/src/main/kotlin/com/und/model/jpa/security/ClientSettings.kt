package com.und.model.jpa.security

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

    @Column(name = "client_id", updatable = false, insertable = false)
    @NotNull
    var clientID: Long? = null

    @Column(name = "timezone", updatable = false, insertable = false)
    var timezone: String = "UTC"

    @Column(name = "authorized_urls")
    var authorizedUrls: String? = null

    @Column(name="android_app_ids")
    var androidAppIds: String?=null

    @Column(name = "ios_app_ids")
    var iosAppIds:String?=null

}