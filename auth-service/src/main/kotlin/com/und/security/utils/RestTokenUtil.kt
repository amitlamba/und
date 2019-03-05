package com.und.security.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.und.common.utils.DateUtils
import com.und.common.utils.loggerFor
import com.und.model.jpa.security.ClientSettings
import com.und.model.jpa.security.UndUserDetails
import com.und.model.jpa.security.User
import com.und.model.redis.security.TokenIdentity
import com.und.model.redis.security.UserCache
import com.und.repository.jpa.security.ClientSettingsRepository
import com.und.repository.jpa.security.UserRepository
import com.und.repository.redis.TokenIdentityRespository
import com.und.service.security.JWTKeyService
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.mobile.device.Device
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.net.URI
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

    @Autowired
    lateinit var clientSettingsRepository: ClientSettingsRepository

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var tokenIdentityRepository: TokenIdentityRespository

    @Value("\${security.expiration}")
    private var expiration: Long = 0


    /**
     * use this method when you just need to validate that token is valid, even if it has been removed from database
     */
    fun validateToken(token: String,value:String?=null): Pair<UndUserDetails?, UserCache> {
        fun getClaimsFromToken(token: String): Pair<Claims,Any?> {

            var jwt=Jwts.parser()
                    .setSigningKeyResolver(keyResolver)
                    .parseClaimsJws(token)
            var body=jwt.body
            var header=jwt.header["TOKEN_ROLE"]
            return Pair(body,header)
        }

        fun buildUserDetails(claims: Claims, jwtDetails: UserCache): UndUserDetails? {
            val userId = claims[CLAIM_USER_ID].toString().toLong()
            return UndUserDetails(
                    id = userId,
                    clientId = claims.clientId?.toLong(),
                    authorities = claims.roles.map { role -> SimpleGrantedAuthority(role) },
                    secret = jwtDetails.secret,
                    username = jwtDetails.username,
                    password = jwtDetails.password,
                    timeZoneId = jwtDetails.timeZoneId

            )
        }

        val claims = getClaimsFromToken(token)
        val userId = claims.first.userId

        var identified=false
        value?.let {
            var idenity=getUniqueIdentityFromRedis(token)
            if(idenity.isEmpty()){
                var user=clientSettingsRepository.findByclientID(claims.first.clientId?.toLong()?:-1)
                if(user.isPresent){
                    when (claims.second.toString()) {
                        "EVENT_ANDROID" -> {
                            var appId = user.get().androidAppIds
                            if (appId != null){
                                val v=objectMapper.readValue(appId, Array<String>::class.java)
                                if (v.indexOf(it) >= 0) identified = true
                                tokenIdentityRepository.save(TokenIdentity(token,v))
                            }
                        }

                        "EVENT_IOS" -> {
                            var appId = user.get().iosAppIds
                            if (appId != null){
                                val v=objectMapper.readValue(appId, Array<String>::class.java)
                                if (v.indexOf(it) >= 0) identified = true
                                tokenIdentityRepository.save(TokenIdentity(token,v))
                            }
                        }

                        "EVENT_WEB" -> {
                            var appId = user.get().authorizedUrls
                            if (appId != null){
                                val v=objectMapper.readValue(appId, Array<String>::class.java)
                                if (isInDomains(v,it)) identified = true
                                tokenIdentityRepository.save(TokenIdentity(token,v))
                            }

                        }
                    }
                }

            }else{
                if(idenity.indexOf(it)>=0) identified=true
            }
        }

        return if (userId != null) {
            val jwtDetails = getJwtIfExists(userId.toLong())
            jwtDetails.identified=identified
            if(claims.second!=null) jwtDetails.tokenKeyType=claims.second.toString()
            val userDetails = buildUserDetails(claims.first, jwtDetails)
            if (!claims.first.isTokenExpired) Pair(userDetails, jwtDetails) else Pair(null, jwtDetails)
        } else Pair(null, UserCache())

    }


    /**
     * use this method when you need to validate that token is valid as well as exists in database
     */
    fun validateTokenForKeyType(token: String, keyType: KEYTYPE,value:String?=null): Pair<UndUserDetails?, UserCache> {
        val (user, jwtDetails) = validateToken(token,value)
        var ktype=keyType
        jwtDetails.tokenKeyType?.let { ktype=KEYTYPE.valueOf(it)}
        val matches: Boolean = when (ktype) {
            KEYTYPE.ADMIN_LOGIN -> jwtDetails.loginKey == token
            KEYTYPE.EVENT_WEB -> jwtDetails.loginKey ==token && jwtDetails.identified
            KEYTYPE.EVENT_IOS -> jwtDetails.iosKey ==token && jwtDetails.identified
            KEYTYPE.EVENT_ANDROID -> jwtDetails.androidKey ==token && jwtDetails.identified
            KEYTYPE.PASSWORD_RESET -> jwtDetails.pswrdRstKey == token
            KEYTYPE.REGISTRATION -> jwtDetails.emailRgstnKey == token
            else -> false
        }
        return if (user != null && matches) Pair(user, jwtDetails) else Pair(null, jwtDetails)

    }

    fun getUniqueIdentityFromRedis(token:String):Array<String>{
        val r=tokenIdentityRepository.findById(token)
        if(r.isPresent) return r.get().identity
        else return emptyArray()
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
    fun retrieveJwtByUser(user: User): UserCache? {
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
        val cachedJwt = user.id?.let{uid-> getJwtIfExists(uid)}?:UserCache()
        //TODO here event user token is generated.
        val jwt = if(user.userType == AuthenticationUtils.USER_TYPE_EVENT ) {
            val expirationDate = Date(Long.MAX_VALUE )
            JWTGenerator(expirationDate, cachedJwt, user).generateJwtByUserDetails(keyType)
        } else if(user.userType == AuthenticationUtils.USER_TYPE_SYSTEM) {

            val expirationDate = Date(Long.MAX_VALUE )
            JWTGenerator(expirationDate, cachedJwt, user).generateJwtForSystemUser(KEYTYPE.PASSWORD_RESET)
        }
        else {
            val expirationDate = Date(dateUtils.now().time + expiration*1000 )
            JWTGenerator(expirationDate, cachedJwt, user).generateJwtByUserDetails(keyType)
        }
        jwtKeyService.save(jwt)
        return jwt

    }

    private fun isInDomains(url1s: Array<String>, url2: String): Boolean {
        url1s.forEach { if(matchDomains(it,url2)) return true }
        return false
    }

    private fun matchDomains(url1: String, url2: String): Boolean {
        val uri1 = URI(url1)
        val domain1 = uri1.host
        val domainWoWww1 = if (domain1.startsWith("www.")) domain1.substring(4) else domain1
        val scheme1 = uri1.scheme
        val uri2 = URI(url2)
        val domain2 = uri2.host
        val domainWoWww2 = if (domain2.startsWith("www.")) domain2.substring(4) else domain2
        val scheme2 = uri2.scheme
        return scheme1 == scheme2 && domainWoWww1 == domainWoWww2
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
    ADMIN_LOGIN,
    EVENT_ANDROID,
    EVENT_IOS,
    EVENT_WEB,PASSWORD_RESET,
    REGISTRATION;
}