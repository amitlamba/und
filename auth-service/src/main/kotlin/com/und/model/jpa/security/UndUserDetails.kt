package com.und.model.jpa.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User

/**
 * Created by shiv on 21/07/17.
 */
class UndUserDetails(
        // @get:JsonIgnore
        val id: Long?,

        val clientId: Long?,

        private val username: String,

        val firstname: String? = "",

        val lastname: String? = "",

        private var password: String? = null,

        val email: String? = null,

        private val authorities: Collection<GrantedAuthority> = arrayListOf(),

        private val enabled: Boolean = false,

        val secret: String,

        val key: String? = null,

        val userType: Int? = null
) : User(username, password, authorities) {

    override fun getUsername(): String {
        return username
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun getPassword(): String? {
        return password
    }

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return authorities
    }

    override fun isEnabled(): Boolean {
        return enabled
    }
}
