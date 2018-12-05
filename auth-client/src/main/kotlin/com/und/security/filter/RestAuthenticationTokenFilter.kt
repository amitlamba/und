package com.und.security.filter

import com.und.security.model.Response
import com.und.security.model.ResponseStatus
import com.und.security.model.UndUserDetails
import com.und.security.service.AuthenticationServiceClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class RestAuthenticationTokenFilter : OncePerRequestFilter() {


    @Autowired
    private lateinit var authenticationService: AuthenticationServiceClient

    @Value("\${security.header.token}")
    private lateinit var tokenHeader: String


    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val authToken = request.getHeader(this.tokenHeader)
        if (SecurityContextHolder.getContext().authentication == null && authToken != null) {
            logger.info("checking authentication for token $authToken ")
//            val auth = authenticationService.validateToken(authToken)
            val validationResponse: Response<UndUserDetails> = authenticationService.validateToken(authToken)
            if (validationResponse.status == ResponseStatus.SUCCESS) {
                val userDetails = validationResponse.data.value
                if (userDetails != null) {
                    val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                    authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                    logger.info("authenticated user usernameFromToken, setting security context")
                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        }

        chain.doFilter(request, response)
    }
}