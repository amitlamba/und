package com.und.web.security.filter

import com.und.security.utils.KEYTYPE
import com.und.security.utils.RestTokenUtil
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
    lateinit  var restTokenUtil: RestTokenUtil

    @Value("\${security.header.token}")
    lateinit  var tokenHeader: String


    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val authToken = request.getHeader(this.tokenHeader)

        if (SecurityContextHolder.getContext().authentication == null && authToken != null) {
            logger.info("checking authentication for token $authToken ")


            val (userDetails, _) = restTokenUtil.validateTokenForKeyType(authToken, KEYTYPE.LOGIN)
            if (userDetails!=null) {
                val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                logger.info("authenticated user usernameFromToken, setting security context")
                SecurityContextHolder.getContext().authentication = authentication
            }
        }

        chain.doFilter(request, response)
    }
}