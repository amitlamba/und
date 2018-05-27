package com.und.security.utils

import com.und.common.utils.DateUtils
import io.jsonwebtoken.Claims
import java.util.*

val Claims.username: String
    get() {
        return this.subject
    }

val Claims.creationDate: Date
    get() {
        return Date(this[RestTokenUtil.CLAIM_KEY_CREATED] as Long)
    }

val Claims.expirationDate: Date
    get() {
        return this.expiration
    }

val Claims.claimedAudience: String
    get() {
        return this[RestTokenUtil.CLAIM_KEY_AUDIENCE] as String
    }
val Claims.isTokenExpired: Boolean
    get() {
        return this.expiration.before(DateUtils().now())
    }

val Claims.ignoreTokenExpiration: Boolean
    get() {
        return RestTokenUtil.AUDIENCE_TABLET == this.claimedAudience || RestTokenUtil.AUDIENCE_MOBILE == this.claimedAudience
    }

val Claims.roles :ArrayList<String>
    get() {
        return this[RestTokenUtil.CLAIM_ROLES] as ArrayList<String>
    }


val Claims.clientId :String?
    get() {
        return this[RestTokenUtil.CLAIM_CLIENT_ID] as String?
    }

val Claims.userId :String?
    get() {
        return this[RestTokenUtil.CLAIM_USER_ID] as String?
    }