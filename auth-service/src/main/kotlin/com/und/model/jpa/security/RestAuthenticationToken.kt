package com.und.model.jpa.security

import com.und.model.jpa.security.UndUserDetails
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority


class RestAuthenticationToken : UsernamePasswordAuthenticationToken {

    var key: String

    constructor(principal: UndUserDetails, credentials: String,
                authorities: Collection<GrantedAuthority>, key: String) : super(principal, credentials, authorities) {
        this.key = key
    }

}
