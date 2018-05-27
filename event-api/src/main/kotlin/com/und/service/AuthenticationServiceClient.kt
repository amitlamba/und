package com.und.security.service

//import org.springframework.cloud.openfeign.FeignClient
import com.und.security.model.AuthenticationRequest
import com.und.security.model.Response
import com.und.security.model.UndUserDetails
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod


@FeignClient("auth-service")
interface AuthenticationServiceClient {

    @RequestMapping(method = arrayOf(RequestMethod.POST), value = "/auth", consumes = arrayOf("application/json"))
    fun authenticate(authenticationRequest: AuthenticationRequest): Response<UndUserDetails>

    @RequestMapping(method = arrayOf(RequestMethod.GET), value = "/auth/validate/{token}/", consumes = arrayOf("application/json"))
    fun validateToken(@PathVariable("token") token: String): Response<UndUserDetails>

    @RequestMapping(method = arrayOf(RequestMethod.GET), value = "/auth/userdetail/{name}/", consumes = arrayOf("application/json"))
    fun userByName(@PathVariable("name") name: String): Response<UndUserDetails>

}