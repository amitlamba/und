package com.und.model.jpa.security

import org.hibernate.annotations.DynamicUpdate
import javax.persistence.*

@Entity
@Table(name = "APPUSER")
@DynamicUpdate(true)
class User {

    @Id
    @Column(name = "ID")
    var id: Long? = null

    @Column(name = "client_id", updatable = false, insertable = false)
    var client: Long? = null

    @Column(name = "user_type", updatable = false, insertable = false)
    var userType: String? = null

    @Column(name = "KEY", length = 255, updatable = false, insertable = false)
    var key: String? = null

    @Column(name="ANDROIDKEY",length = 255)
    var androidKey:String?=null

    @Column(name = "IOSKEY",length = 255)
    var iosKey:String?=null
}