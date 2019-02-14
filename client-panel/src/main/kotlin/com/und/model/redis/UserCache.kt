package com.und.model.redis

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

@RedisHash("user")
class UserCache {

    @Id
    lateinit var userId:String
    var loginKey:String?= null
    lateinit var clientId:String
}