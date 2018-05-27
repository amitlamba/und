package com.und.security.utils

import com.und.common.utils.DateUtils
import com.und.common.utils.loggerFor
import com.und.model.jpa.security.UndUserDetails
import com.und.model.jpa.security.User
import com.und.model.redis.security.UserCache
import com.und.service.security.JWTKeyService
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mobile.device.Device
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@Component
class RestTokenUtil {

    @Autowired
    private lateinit var dateUtils: DateUtils

    @Autowired
    private lateinit var keyResolver: KeyResolver

    @Autowired
    private lateinit var jwtKeyService: JWTKeyService

    @Value("\${security.expiration}")
    private var expiration: Long = 0


    /**
     * use this method when you just need to validate that token is valid, even if it has been removed from database
     */
    fun validateToken(token: String): Pair<UndUserDetails?, UserCache> {
        fun getClaimsFromToken(token: String): Claims {
            return Jwts.parser()
                    .setSigningKeyResolver(keyResolver)
                    .parseClaimsJws(token)
                    .body

        }

        fun buildUserDetails(claims: Claims, jwtDetails: UserCache): UndUserDetails? {
            val userId = claims[CLAIM_USER_ID].toString().toLong()
            return UndUserDetails(
                    id = userId,
                    clientId = claims.clientId?.toLong(),
                    authorities = claims.roles.map { role -> SimpleGrantedAuthority(role) },
                    secret = jwtDetails.secret,
                    username = jwtDetails.username,
                    password = jwtDetails.password
            )
        }

        val claims = getClaimsFromToken(token)
        val userId = claims.userId
        return if (userId != null) {
            val jwtDetails = getJwtIfExists(userId.toLong())
            val userDetails = buildUserDetails(claims, jwtDetails)
            if (!claims.isTokenExpired) Pair(userDetails, jwtDetails) else Pair(null, jwtDetails)
        } else Pair(null, UserCache())

    }


    /**
     * use this method when you need to validate that token is validas well as exists in database
     */
    fun validateTokenForKeyType(token: String, keyType: KEYTYPE): Pair<UndUserDetails?, UserCache> {
        val (user, jwtDetails) = validateToken(token)
        val matches: Boolean = when (keyType) {
            KEYTYPE.LOGIN -> jwtDetails.loginKey == token
            KEYTYPE.PASSWORD_RESET -> jwtDetails.pswrdRstKey == token
            KEYTYPE.REGISTRATION -> jwtDetails.emailRgstnKey == token
        }
        return if (user != null && matches) Pair(user, jwtDetails) else Pair(null, jwtDetails)

    }

    fun getJwtIfExists(userId: Long): UserCache {
        return jwtKeyService.getKeyIfExists(userId)
    }

    fun updateJwt(jwt: UserCache): UserCache {
        return jwtKeyService.updateJwt(jwt)
    }



    /**
     * used to generate a token for keytype options,
     * user object should have, id, secret, username and password present
     */
    fun retrieveJwtByUser(user: User, keyType: KEYTYPE): UserCache? {
        val userDetails = RestUserFactory.create(user)
        return if (userDetails.id != null) getJwtIfExists(userDetails.id) else null
    }

    /**
     * used to generate a token for keytype options,
     * user object should have, id, secret, username and password present
     */
    fun generateJwtByUser(user: User, keyType: KEYTYPE): UserCache {
        user.userType
        val userDetails = RestUserFactory.create(user)
        val jwt = generateJwtByUser(userDetails, keyType)
        return jwt
    }

    /**
     * used to generate a token for keytype options,
     * userDetails object should have, id, secret, username and password present
     * tries to get jwt object from cache, and updates requested key type if it exists else makes a new entry
     */
    fun generateJwtByUser(user: UndUserDetails, keyType: KEYTYPE): UserCache {
        val cachedJwt = if (user.id != null) getJwtIfExists(user.id) else UserCache()
        val jwtGenerator = if(user.userType == AuthenticationUtils.USER_TYPE_EVENT) {
            val expirationDate = Date(Long.MAX_VALUE )
            JWTGenerator(expirationDate, cachedJwt, user)
        } else {
            val expirationDate = Date(dateUtils.now().time + expiration*1000 )
            JWTGenerator(expirationDate, cachedJwt, user)
        }
        val jwt = jwtGenerator.generateJwtByUserDetails(keyType)
        jwtKeyService.save(jwt)
        return jwt

    }


    companion object {
        protected val logger = loggerFor(RestTokenUtil::class.java)
        private const val serialVersionUID = -3301605591108950415L

        internal val CLAIM_KEY_USERNAME = "sub"
        internal val CLAIM_KEY_AUDIENCE = "audience"
        internal val CLAIM_KEY_CREATED = "created"
        internal val CLAIM_KEY_EXPIRED = "exp"
        internal val CLAIM_ONE_TIME = "onetime"
        internal val CLAIM_ROLES = "roles"
        internal val CLAIM_CLIENT_ID = "clientId"
        internal val CLAIM_USER_ID = "userId"
        internal val AUDIENCE_UNKNOWN = "unknown"
        internal val AUDIENCE_WEB = "web"
        internal val AUDIENCE_MOBILE = "mobile"
        internal val AUDIENCE_TABLET = "tablet"
    }
}

enum class KEYTYPE {
    LOGIN, PASSWORD_RESET, REGISTRATION
}