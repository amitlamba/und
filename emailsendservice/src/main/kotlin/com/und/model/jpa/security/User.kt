package com.und.model.jpa.security

import org.hibernate.annotations.DynamicUpdate
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

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
    var userType: Int? = null

    @Column(name = "KEY", length = 255, updatable = false, insertable = false)
    var key: String? = null




}