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
import java.util.regex.Pattern
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
        var type=request.getHeader("type")
        var t="ADMIN_LOGIN"
        var v:String=""
        when(type){
            "WEB"-> { t="EVENT_WEB" ;
                v="${request.getHeader("referer")}"
                var pattern= Pattern.compile("^(?<scheme>https?)(:\\/\\/)(?<host>\\w+(\\.\\w+)+\\/?)")
                var matcher=pattern.matcher(v)
                if(matcher.find()){
                    var scheme=matcher.group("scheme")
                    var host=matcher.group("host")
                    v="$scheme://$host"
                }else{
                    logger.info("Referer format not match $v")
                }
            }
            "ANDROID"->{ t="EVENT_ANDROID" ; v=request.getHeader("androidAppId")}
            "IOS"->{ t="EVENT_IOS" ; v=request.getHeader("iosAppId")}
        }

        logger.info("Remote Address ${request.getRemoteAddr()}")
        logger.info("Remote Host ${request.getRemoteHost()}")
        logger.info("Server Name ${request.getServerName()}")
        logger.info("Request Url ${request.getRequestURL()}")
        logger.info("Scheme ${request.getScheme()}")
        var headers=request.headerNames
        while (headers.hasMoreElements()){
            var name=headers.nextElement()
            println("$name ${request.getHeader(name)}")
        }

        logger.info("Request for type $t Identity(Host/AppId) is $v")
        val authToken = request.getHeader(this.tokenHeader)
        if (SecurityContextHolder.getContext().authentication == null && authToken != null) {
            logger.info("checking authentication for token $authToken ")
            var validationResponse: Response<UndUserDetails>
                    if(t.equals("ADMIN_LOGIN"))  validationResponse=authenticationService.validateToken(authToken,null,null)
                    else validationResponse=authenticationService.validateToken(authToken,t,v)
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

enum class KEYTYPE {
    PASSWORD_RESET, REGISTRATION;
    enum class LOGIN {
        ADMIN_LOGIN,
        EVENT_ANDROID,
        EVENT_IOS,
        EVENT_WEB

    }
}