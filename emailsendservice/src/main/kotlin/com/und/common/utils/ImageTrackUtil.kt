package com.und.common.utils

import org.springframework.beans.factory.annotation.Value
import java.net.URLEncoder

class ImageTrackUtil {

    @Value("\${und.url.event}")
    private lateinit var eventApiUrl: String

    fun getImageUrl(clientId: Int, mongoEmailId: String): String {
        val id = clientId.toString()+"###"+mongoEmailId
        return getImageUrl(id)
    }

    fun getImageUrl(id: String): String {
        return "$eventApiUrl/email/image/${URLEncoder.encode(URLEncoder.encode(encrypt(id), "UTF-8"), "UTF-8")}.jpg"
    }
}