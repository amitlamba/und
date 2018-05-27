package com.und.security.utils

import com.und.service.security.JWTKeyService
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwsHeader
import io.jsonwebtoken.SigningKeyResolverAdapter
import io.jsonwebtoken.impl.TextCodec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class KeyResolver : SigningKeyResolverAdapter() {

    @Autowired
    lateinit var jwtKeyService: JWTKeyService

    override fun resolveSigningKeyBytes(header: JwsHeader<out JwsHeader<*>>?, payload: Claims?): ByteArray {
        return if (payload != null) {
            getSecret(
                    payload[RestTokenUtil.CLAIM_CLIENT_ID] as String?,
                    payload[RestTokenUtil.CLAIM_USER_ID] as String?
            )
        } else {
            byteArrayOf()
        }
    }

    private fun getSecret(clientId: String?, userId: String?): ByteArray {
        //TODO FIXME implement db connection here to get data of key, same for below method as well
        return if (clientId != null && userId != null) {
            val jwtKey = jwtKeyService.getKeyIfExists(userId.toLong())
            TextCodec.BASE64.decode(jwtKey.secret)
        } else byteArrayOf()
    }
}