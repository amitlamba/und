package com.und.web.controller.security

import com.und.common.utils.loggerFor
import com.und.web.model.Data
import com.und.web.model.Response
import com.und.web.model.ResponseStatus
import com.und.model.jpa.security.RestAuthenticationRequest
import com.und.model.jpa.security.UndUserDetails
import com.und.service.security.SecurityAuthenticationResponse
import com.und.security.utils.KEYTYPE
import com.und.security.utils.RestTokenUtil
import com.und.service.security.captcha.CaptchaService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.client.AbstractClientHttpResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@CrossOrigin
@RestController
class AuthenticationRestController {


    @Autowired
    private lateinit var authenticationManager: AuthenticationManager

    @Autowired
    private lateinit var userDetailsService: UserDetailsService

    @Autowired
    private lateinit var restTokenUtil: RestTokenUtil

    @Autowired
    private lateinit var captchaService: CaptchaService

    companion object {
        var logger=LoggerFactory.getLogger(AuthenticationRestController::class.java)
    }
    @PostMapping(value = ["\${security.route.authentication.path}"])
    @Throws(AuthenticationException::class)
    fun createAuthenticationToken(@RequestBody authenticationRequest: RestAuthenticationRequest, request: HttpServletRequest): ResponseEntity<*> {
        fun generateJwtByUser(username: String): String {
            // Reload password post-security so we can generate token
            val user: UndUserDetails? = userDetailsService.loadUserByUsername(username) as UndUserDetails
            return if (user != null) {
                restTokenUtil.generateJwtByUser(user, KEYTYPE.ADMIN_LOGIN).loginKey ?: ""
            } else ""
        }

        val response = request.getParameter("recaptchaToken")
        captchaService.processResponse(response)
        val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(
                        authenticationRequest.username,
                        authenticationRequest.password
                )
        )
        SecurityContextHolder.getContext().authentication = authentication
        val token = generateJwtByUser(authenticationRequest.username ?: "")
        return ResponseEntity.ok(
                Response(
                        status = ResponseStatus.SUCCESS,
                        data = Data(SecurityAuthenticationResponse(token))
                )
        )


    }

    @GetMapping(value = ["\${security.route.authentication.path}/validate/{authToken}"])
    @Throws(AuthenticationException::class)
    fun authenticationToken(@PathVariable("authToken", required = true) authToken: String, request: HttpServletRequest): ResponseEntity<*> {
        val type:String? = request.getParameter("type")
        val value:String? = request.getParameter("value")
        logger.info("Validating token type $type and value $value")
        var result: Pair<com.und.model.jpa.security.UndUserDetails?, com.und.model.redis.security.UserCache>? = null
        if (type != null && value != null) {
            when (KEYTYPE.valueOf(type)) {
                KEYTYPE.EVENT_ANDROID -> result = restTokenUtil.validateTokenForKeyType(authToken, KEYTYPE.EVENT_ANDROID, value)
                KEYTYPE.EVENT_IOS -> result = restTokenUtil.validateTokenForKeyType(authToken, KEYTYPE.EVENT_IOS, value)
                KEYTYPE.EVENT_WEB -> result = restTokenUtil.validateTokenForKeyType(authToken, KEYTYPE.EVENT_WEB, value)
            }
        } else {
            result = restTokenUtil.validateTokenForKeyType(authToken, KEYTYPE.ADMIN_LOGIN)
        }
        return if (result?.first?.id != null) {
            ResponseEntity.ok(Response(
                    status = ResponseStatus.SUCCESS,
                    data = Data(result.first)

            ))
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Response(
                    status = ResponseStatus.FAIL,
                    message = "Invalid Authentication Attempt"
            ))
        }

    }


    @PreAuthorize("hasRole(ROLE_SYSTEM)")
    @GetMapping(value = ["\${security.route.authentication.path}/userdetail/{name}"])
    @Throws(AuthenticationException::class)
    fun userByName(@PathVariable("name") name: String): ResponseEntity<*> {
        //FIXME check for authentication token of service in header
        val userDetails = userDetailsService.loadUserByUsername(name)
        return if (userDetails?.username != null) {
            ResponseEntity.ok(Response(
                    status = ResponseStatus.SUCCESS,
                    data = Data(userDetails)

            ))
        } else {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Response(
                    status = ResponseStatus.FAIL,
                    message = "Invalid Authentication Attempt"
            ))
        }

    }


}
