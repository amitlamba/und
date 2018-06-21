package com.und.security.service

import com.und.security.model.Response
import com.und.security.model.AuthenticationRequest
import com.und.security.model.UndUserDetails
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*


@FeignClient("auth-service")
interface AuthenticationServiceClient {

/*    @PostMapping(value = ["/auth"], consumes = ["application/json"])
    fun authenticate(authenticationRequest: AuthenticationRequest): Response<UndUserDetails>*/

    @GetMapping(value = ["/auth/validate/{token}/"], consumes = ["application/json"])
    fun validateToken(@PathVariable("token") token: String): Response<UndUserDetails>

    @GetMapping(value = ["/auth/userdetail/{name}/"], consumes = ["application/json"])
    fun userByName(@PathVariable("name") name: String, @RequestHeader("Authorization") token: String): Response<UndUserDetails>

}