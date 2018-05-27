package com.und.service.security

import java.io.Serializable

/**
 * Created by shiv on 21/07/17.
 */
class SecurityAuthenticationResponse(val token: String?) : Serializable {
    companion object {

        private const val serialVersionUID = 1250166508152483573L
    }
}
