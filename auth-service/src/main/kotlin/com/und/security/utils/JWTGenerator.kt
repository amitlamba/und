package com.und.security.utils

import com.und.common.utils.DateUtils
import com.und.model.jpa.security.UndUserDetails
import com.und.model.redis.security.UserCache
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import java.util.*

class JWTGenerator(private val expirationDate: Date,
                   private val cachedJwt: UserCache,
                   private val user: UndUserDetails) {

    private val dateUtils: DateUtils = DateUtils()

    fun generateJwtByUserDetails(keyType: KEYTYPE): UserCache {
        cachedJwt.userId = "${user.id}"
        when (keyType) {
            KEYTYPE.LOGIN -> cachedJwt.loginKey = generateToken(user)
            KEYTYPE.PASSWORD_RESET -> cachedJwt.pswrdRstKey = generateToken(user)
            KEYTYPE.REGISTRATION -> cachedJwt.emailRgstnKey = generateToken(user)
        }
        cachedJwt.secret = user.secret
        cachedJwt.username = user.username
        cachedJwt.firstname = user.firstname
        cachedJwt.lastname = user.lastname
        cachedJwt.password = user.password!!
        cachedJwt.email = user.email ?: "Notfound"
        cachedJwt.clientId = "${user.clientId}"
        return cachedJwt
    }

    fun generateJwtForSystemUser(keyType: KEYTYPE): UserCache {
        cachedJwt.userId = "${user.id}"
        when (keyType) {
            KEYTYPE.LOGIN -> cachedJwt.loginKey = user.key
            KEYTYPE.PASSWORD_RESET -> cachedJwt.pswrdRstKey = generateToken(user)
            KEYTYPE.REGISTRATION -> cachedJwt.emailRgstnKey = generateToken(user)
        }
        cachedJwt.secret = user.secret
        cachedJwt.username = user.username
        cachedJwt.password = user.password!!
        cachedJwt.email = user.email ?: "Notfound"
        cachedJwt.clientId = "1"
        return cachedJwt
    }


    private fun generateToken(userDetails: UndUserDetails): String {

        val createdDate = dateUtils.now()

        val claims = mapOf(
                RestTokenUtil.CLAIM_KEY_USERNAME to userDetails.username,
                //CLAIM_KEY_AUDIENCE to audience,
                RestTokenUtil.CLAIM_USER_ID to userDetails.id.toString(),
                RestTokenUtil.CLAIM_CLIENT_ID to userDetails.clientId.toString(),
                RestTokenUtil.CLAIM_ROLES to userDetails.authorities.map { auth -> auth.authority },
                RestTokenUtil.CLAIM_KEY_CREATED to createdDate
        )


        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, userDetails.secret)
                .compact()

    }


}