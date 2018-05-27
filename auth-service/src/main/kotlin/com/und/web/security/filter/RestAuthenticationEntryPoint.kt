package com.und.web.security.filter

import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io.IOException
import java.io.Serializable
import javax.servlet.ServletException



@Component
class RestAuthenticationEntryPoint : AuthenticationEntryPoint, Serializable {

    @Throws(IOException::class, ServletException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    override fun commence(request: HttpServletRequest,
                          response: HttpServletResponse,
                          authException: AuthenticationException) {
        // This is invoked when user tries to access a secured REST resource without supplying any credentials
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
    }


    @ExceptionHandler( AccessDeniedException::class )
    @Throws(IOException::class)
    fun commence(request: HttpServletRequest, response: HttpServletResponse,
                 accessDeniedException: AccessDeniedException) {
        // 403
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Authorization Failed :  ${accessDeniedException.message}")
    }

    @ExceptionHandler( Exception::class )
    @Throws(IOException::class)
    fun commence(request: HttpServletRequest, response: HttpServletResponse,
                 exception: Exception) {
        // 500
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error : ${exception.message}")
    }

    companion object {

        private const val serialVersionUID = -8970718410437077606L
    }
}