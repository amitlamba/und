package com.und.security.utils

import org.springframework.stereotype.Component

@Component
class TenantProvider {
    val local : ThreadLocal<String> = ThreadLocal.withInitial { "" }


    val tenant: String
        get() = if (AuthenticationUtils.isUserLoggedIn) AuthenticationUtils.id else local.get()

    fun setTenat(id : String) {
        local.set(id)
    }
}
