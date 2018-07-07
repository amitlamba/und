package com.und.security.service

import com.und.security.model.Response
import com.und.security.model.AuthenticationRequest
import com.und.security.model.UndUserDetails
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.*


@FeignClient("auth-service")
interface AuthenticationServiceClient {

    @GetMapping(value = ["/auth/validate/{token}/"], consumes = ["application/json"])
    fun validateToken(@PathVariable("token") token: String): Response<UndUserDetails>


}