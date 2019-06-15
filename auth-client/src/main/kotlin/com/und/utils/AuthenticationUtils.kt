package com.und.security.utils

import com.und.security.model.UndUserDetails
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

object AuthenticationUtils {

    val isUserLoggedIn: Boolean
        get() {
            val securityContext = SecurityContextHolder.getContext() ?: return false

            val authentication = securityContext.authentication
            return !(authentication == null || !authentication.isAuthenticated || authentication is AnonymousAuthenticationToken)
        }

    val principal: UndUserDetails
        get() {
            val securityContext = SecurityContextHolder.getContext() ?: throw AccessDeniedException("User is not logged in to the system.")

            val authentication = securityContext.authentication ?: throw AccessDeniedException("User is not logged in to the system.")

            return authentication.principal as UndUserDetails
        }


    val name: String?
        get() {
            val principal = principal
            return (principal as? UndUserDetails)?.username
        }

    val id: String
        get() {
            return principal.id.toString()
        }

    val clientID: Long?
        get() {
            return principal.clientId
        }

    fun  retrieveClientId(): Long {
        return clientID?: throw AccessDeniedException("Access Denied.")
    }


}