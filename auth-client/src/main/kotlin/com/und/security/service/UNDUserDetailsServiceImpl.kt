package com.und.security.service

import com.und.security.model.ResponseStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
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
    private lateinit var authenticationService: AuthenticationServiceClient

    @Value("\${und.system.user.token}")
    private lateinit var systemToken:String

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val response = authenticationService.userByName(username, systemToken)
        return if (response.status == ResponseStatus.SUCCESS) {
            response.data.value as UserDetails
        } else {
            throw UsernameNotFoundException("No user found with username $username.")
        }
    }


}
