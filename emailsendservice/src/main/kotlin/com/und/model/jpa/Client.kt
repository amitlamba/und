package com.und.model.jpa

import com.und.model.jpa.security.User
import org.hibernate.annotations.DynamicUpdate
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

@Entity
@Table(name = "CLIENT")
class Client {

    @Id
    @Column(name = "ID")
    var id: Long? = null

    //TODO CREATE STATE COLUMN

    @Column(name = "NAME", length = 255)
    lateinit var name: String

    @Column(name = "EMAIL", length = 255, unique = true)
    lateinit var email: String

    @Column(name = "PHONE", length = 50)
    var phone: String? = null


    @Column(name = "firstname")
    var firstname: String? = null

    @Column(name = "lastname")
    var lastname: String? = null



}