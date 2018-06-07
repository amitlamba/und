package com.und.security.utils

import org.springframework.stereotype.Component

@Component
class TenantProvider {
    val local: ThreadLocal<String> = ThreadLocal.withInitial { "" }


    val tenant: String
        get() {
            return if (AuthenticationUtils.isUserLoggedIn) {
                val clientId = AuthenticationUtils.clientID
                clientId.toString()
            } else {
                local.get()
            }
        }

    fun setTenat(id: String) {
        local.set(id)
    }
}
