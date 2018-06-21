package com.und.service.security.captcha


import com.und.web.controller.exception.ReCaptchaInvalidException
import com.und.web.controller.exception.ReCaptchaUnavailableException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestOperations
import java.net.URI
import java.util.regex.Pattern
import javax.servlet.http.HttpServletRequest

@Service("captchaService")
class CaptchaService : ICaptchaService {

    @Autowired
    private lateinit var request: HttpServletRequest

    @Autowired
    private lateinit var captchaSettings: CaptchaSettings

    @Autowired
    private lateinit var reCaptchaAttemptService: ReCaptchaAttemptService

    @Autowired
    private lateinit var restTemplate: RestOperations

    @Value("\${system.captcha.enabled}")
    private var captchaEnabled: Boolean = true

    override val reCaptchaSite: String?
        get() = captchaSettings.site

    override val reCaptchaSecret: String?
        get() = captchaSettings.secret

    private val clientIP: String
        get() {
            val xfHeader = request.getHeader("X-Forwarded-For") ?: return request.remoteAddr
            return xfHeader.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
        }

    override fun processResponse(response: String) {
        LOGGER.debug("Attempting to validate response {}", response)
        if (captchaEnabled) {
            if (reCaptchaAttemptService.isBlocked(clientIP)) {
                throw ReCaptchaInvalidException("Client exceeded maximum number of failed attempts")
            }

            if (!responseSanityCheck(response)) {
                throw ReCaptchaInvalidException("Response contains invalid characters")
            }

            val verifyUri = URI.create(String.format("https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s&remoteip=%s", reCaptchaSecret, response, clientIP))
            try {
                val googleResponse = restTemplate.getForObject(verifyUri, GoogleResponse::class.java)
                LOGGER.debug("Google's response: {} ", googleResponse.toString())

                if (!googleResponse.isSuccess) {
                    if (googleResponse.hasClientError()) {
                        reCaptchaAttemptService.reCaptchaFailed(clientIP)
                    }
                    throw ReCaptchaInvalidException("reCaptcha was not successfully validated")
                }
            } catch (rce: RestClientException) {
                throw ReCaptchaUnavailableException("Registration unavailable at this time.  Please try again later.", rce)
            }

            reCaptchaAttemptService.reCaptchaSucceeded(clientIP)
        }
    }

    private fun responseSanityCheck(response: String): Boolean {
        return StringUtils.hasLength(response) && RESPONSE_PATTERN.matcher(response).matches()
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(CaptchaService::class.java)

        private val RESPONSE_PATTERN = Pattern.compile("[A-Za-z0-9_-]+")
    }
}
