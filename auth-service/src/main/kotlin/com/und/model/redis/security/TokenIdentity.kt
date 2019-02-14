package com.und.model.redis.security

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

@RedisHash("token_identity")
class TokenIdentity {

    constructor()

    constructor(token:String,identity:Array<String>){
        this.token=token
        this.identity=identity
    }

    @Id
    lateinit var token:String

    var identity:Array<String> = emptyArray()
}