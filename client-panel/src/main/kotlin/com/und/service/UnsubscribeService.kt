package com.und.service

import com.und.common.utils.decrypt
import com.und.common.utils.encrypt
import org.springframework.stereotype.Service

@Service
class UnsubscribeService {

    data class UnsubscribeLinkParams(val emailAddress: String?, val clientID: Int, val userID: String?)

    private val separator = "|||"


    //@Value("\${encryption-decryption.key.unsubscribe}")
    private lateinit var encryptDecryptKey: String

    fun createUnsubscribeLink(unsubscribeLinkParams: UnsubscribeLinkParams): String {
        val linkData = unsubscribeLinkParams.let {
            param -> listOf(param.emailAddress, param.clientID, param.userID)
        }
        linkData.filterNotNull().joinToString(separator)
        val subscribeLinkString = linkData.joinToString(separator)
        return  encrypt(subscribeLinkString, key = encryptDecryptKey)
    }

    fun getDataFromUnsubscribeLink(unsubscribeLink: String): UnsubscribeLinkParams {
        val dString = decrypt(stringToDecrypt = unsubscribeLink, key = encryptDecryptKey)
        val arr = dString.split(separator)
        return UnsubscribeLinkParams(arr[0], arr[1].toInt(), arr[2])
    }
}