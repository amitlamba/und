package com.und.model.jpa.security

import com.und.security.utils.AuthenticationUtils
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.DynamicUpdate

import java.util.Date
import javax.persistence.*

import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
@Table(name = "APPUSER")
@DynamicUpdate(true)
class User {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "appuser_id_seq")
    @SequenceGenerator(name = "appuser_id_seq", sequenceName = "appuser_id_seq", allocationSize = 1)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CLIENT_ID")
    var client: Client? = null

    @Column(name = "user_type")
    @NotNull
    var userType: Int = AuthenticationUtils.USER_TYPE_ADMIN

    @Column(name = "USERNAME", length = 255, unique = true)
    @NotNull
    lateinit var username: String

    @Column(name = "PASSWORD", length = 100)
    @NotNull
    lateinit var password: String

    @Column(name = "FIRSTNAME", length = 255)
    @NotNull
    lateinit var firstname: String

    @Column(name = "LASTNAME", length = 255)
    @NotNull
    lateinit var lastname: String

    @Column(name = "EMAIL", length = 255)
    @NotNull
    lateinit var email: String

    @Column(name = "ENABLED")
    @NotNull
    var enabled: Boolean = false

    @Column(name = "LASTPASSWORDRESETDATE")
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    lateinit var lastPasswordResetDate: Date

    @Column(name = "SECRET", length = 255)
    @NotNull
    lateinit var clientSecret: String

    @Column(name = "KEY", length = 255)
    var key: String? = null

    @Column(name = "PHONE", length = 15)
    var mobile: String? = null

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "USER_AUTHORITY", joinColumns = arrayOf(JoinColumn(name = "USER_ID", referencedColumnName = "ID")), inverseJoinColumns = arrayOf(JoinColumn(name = "AUTHORITY_ID", referencedColumnName = "ID")))
    var authorities: List<Authority> = arrayListOf()
}