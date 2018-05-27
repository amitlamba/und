package com.und.model.jpa.security

import java.io.Serializable

/**
 * Created by shiv on 21/07/17.
 */
class RestAuthenticationRequest (
        var username: String? = null,
        var password: String? = null
)

