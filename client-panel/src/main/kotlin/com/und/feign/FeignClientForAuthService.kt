package com.und.feign

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader

//@FeignClient(name="auth-service")
//interface FeignClientForAuthService {
//    @PostMapping(value = ["/setting/refreshToken/{new}/{type}"])
//    fun refreshToken(@PathVariable("new",required = true)isNew:Boolean,
//                     @PathVariable("type",required = true)type:String,@RequestHeader("Authorization")token:String?):ResponseEntity<*>
//}