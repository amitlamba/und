package com.und.utils

import org.springframework.stereotype.Component

@Component
class TenantProvider {
    val local : ThreadLocal<String> = ThreadLocal.withInitial { "" }

    val tenant: String
        get() = local.get()

    fun setTenant(id : String) {
        local.set(id)
    }
}