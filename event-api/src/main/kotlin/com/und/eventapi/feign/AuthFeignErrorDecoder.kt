package com.und.eventapi.feign

//import org.apache.commons.io.IOUtils
import feign.Response
import feign.codec.ErrorDecoder
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.client.HttpServerErrorException

class AuthFeignErrorDecoder : ErrorDecoder {

    private val delegate = ErrorDecoder.Default()

    override fun decode(methodKey: String, response: Response): Exception {
        val responseHeaders = HttpHeaders()
        response.headers().entries.stream()
                .forEach { entry -> responseHeaders.put(entry.key, ArrayList(entry.value)) }

        //you have to first fix it in auth-service as told earlier, to handle exception properly and throw appropriate error code
        val statusCode = HttpStatus.valueOf(response.status())
        val statusText = response.reason()

        val responseBody: ByteArray? = null
        /*try {
            responseBody = IOUtils.toByteArray(response.body().asInputStream())
        } catch (e: IOException) {
            throw RuntimeException("Failed to process response body.", e)
        }*/

        if (response.status() == 401) {
            throw AccessDeniedException(statusText)
        } else
            return if (response.status() in 500..599) {
                HttpServerErrorException(statusCode, statusText, responseHeaders, responseBody, null)
            } else delegate.decode(methodKey, response)
    }
}

