package com.und.security.service

import com.und.security.model.ResponseStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

/**
 * Created by shiv on 21/07/17.
 */
@Service
class UNDUserDetailsServiceImpl : UserDetailsService {

    @Autowired
    lateinit private var authenticationService: AuthenticationServiceClient

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val response = authenticationService.userByName(username)
        return if (response.status == ResponseStatus.SUCCESS) {
            response.data.value as UserDetails
        } else {
            throw UsernameNotFoundException("No user found with username $username.")
        }
    }
}







