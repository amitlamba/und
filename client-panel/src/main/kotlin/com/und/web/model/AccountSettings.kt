package com.und.web.model

import java.util.*

data class AccountSettings(val id: Long? = null, val urls: Array<String>, val timezone: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AccountSettings

        if (!Arrays.equals(urls, other.urls)) return false
        if (timezone != other.timezone) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Arrays.hashCode(urls)
        result = 31 * result + timezone.hashCode()
        return result
    }
}