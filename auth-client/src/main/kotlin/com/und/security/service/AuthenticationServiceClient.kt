package com.und.security.service

import com.und.security.model.Response
import com.und.security.model.AuthenticationRequest
import com.und.security.model.UndUserDetails
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity


@FeignClient(name="auth-service")
interface AuthenticationServiceClient {

    @GetMapping(value = ["/auth/validate/{token}"], consumes = ["application/json"])
    fun validateToken(@PathVariable("token") token: String,
                      @RequestParam("type",required=false)type:String?,
                      @RequestParam("value",required=false)value:String?): Response<UndUserDetails>

    @PostMapping(value = ["/setting/refreshToken/{new}/{type}"])
    fun refreshToken(@PathVariable("new",required = true)isNew:Boolean,
                     @PathVariable("type",required = true)type:String,@RequestHeader("Authorization")token:String?):ResponseEntity<*>

}