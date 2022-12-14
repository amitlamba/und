package com.und.model.redis.security

import com.und.model.jpa.security.Authority
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import java.time.ZoneId

@RedisHash("user")
class UserCache {

    constructor()

    constructor(secret: String, userId: String) {
        this.userId = userId
        this.secret = secret
    }

    @Id
    lateinit var userId: String

    lateinit var  clientId: String

    lateinit var email: String

    lateinit var username: String

    lateinit var secret: String

    lateinit var password: String

    var firstname: String? = null

    var lastname: String? = null

    var authorities: List<Authority> = arrayListOf()

    var enabled: Boolean = true

    var loginKey: String? = null

    var androidKey:String?=null

    var iosKey:String?=null

    var identified:Boolean=false

    var tokenKeyType:String?=null

    var pswrdRstKey: String? = null

    var emailRgstnKey: String? = null

    var timeZoneId:String = ZoneId.of("UTC").id


}