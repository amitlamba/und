package com.und.feign

import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.HttpClientErrorException
import java.io.IOException
import feign.Response
import feign.codec.ErrorDecoder
import org.apache.commons.io.IOUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus


class AuthFeignErrorDecoder : ErrorDecoder {

    private val delegate = ErrorDecoder.Default()

    override fun  decode(methodKey: String, response: Response): Exception {
        val responseHeaders = HttpHeaders()
        response.headers().entries.stream()
                .forEach { entry -> responseHeaders.put(entry.key, ArrayList(entry.value)) }

        val statusCode = HttpStatus.valueOf(response.status())
        val statusText = response.reason()

        val responseBody: ByteArray
        try {
            responseBody = IOUtils.toByteArray(response.body().asInputStream())
        } catch (e: IOException) {
            throw RuntimeException("Failed to process response body.", e)
        }

        if (response.status() in 400..499) {
            throw HttpClientErrorException(statusCode, statusText, responseHeaders, responseBody, null)
        }

        return if (response.status() in 500..599) {
            HttpServerErrorException(statusCode, statusText, responseHeaders, responseBody, null)
        } else delegate.decode(methodKey, response)
    }
}