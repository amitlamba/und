package com.und.common.utils

import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


private val aes = "AES"
private var keyDefault = "BaO1TXt5B0R92Yys" // 128 bit key


fun encrypt(stringToEncrypt: String, key: String? = null): String {
    val keyVal = key ?: keyDefault
    val aesKey = SecretKeySpec(keyVal.toByteArray(), aes)
    val cipher = Cipher.getInstance(aes)
    cipher.init(Cipher.ENCRYPT_MODE, aesKey)
    val encryptedByteArray = cipher.doFinal(stringToEncrypt.toByteArray())
    return String(Base64.getEncoder().encode(encryptedByteArray))
}

fun decrypt(stringToDecrypt: String, key: String? = null): String {
    val keyVal = key ?: keyDefault
    val aesKey = SecretKeySpec(keyVal.toByteArray(), aes)
    val cipher = Cipher.getInstance(aes)
    cipher.init(Cipher.DECRYPT_MODE, aesKey)
    return String(cipher.doFinal(Base64.getDecoder().decode(stringToDecrypt.toByteArray())))
}