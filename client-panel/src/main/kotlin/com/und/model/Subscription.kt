package com.und.model

import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.spec.ECPublicKeySpec
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.util.*


class Subscription {
    var auth: String? = null
    var key: String? = null
    var endPoint: String? = null

    /**
     * Returns the base64 encoded auth string as a byte[]
     */
    fun getAuthAsBytes(): ByteArray {
        return Base64.getDecoder().decode(auth)
    }

    /**
     * Returns the base64 encoded public key string as a byte[]
     */
    fun getKeyAsBytes(): ByteArray {
        return Base64.getDecoder().decode(key)
    }

    /**
     * Returns the base64 encoded public key as a PublicKey object
     */
    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class, NoSuchProviderException::class)
    fun getUserPublicKey(): PublicKey {
        val kf = KeyFactory.getInstance("ECDH", BouncyCastleProvider.PROVIDER_NAME)
        val ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1")
        val point = ecSpec.getCurve().decodePoint(getKeyAsBytes())
        val pubSpec = ECPublicKeySpec(point, ecSpec)

        return kf.generatePublic(pubSpec)
    }
}