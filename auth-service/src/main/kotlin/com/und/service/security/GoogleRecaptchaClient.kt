/*
package com.und.security.service

import com.fasterxml.jackson.annotation.JsonProperty
import feign.Body
import feign.Headers
import feign.Param
import feign.QueryMap
import feign.form.FormEncoder
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.cloud.openfeign.support.SpringEncoder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Scope
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.beans.Encoder
import java.time.LocalDateTime
import javax.ws.rs.core.MultivaluedMap

//@Service
@FeignClient(name="GoogleRecaptchaClient", url = "https://www.google.com/recaptcha")
interface GoogleRecaptchaClient {

    @PostMapping(value = "/api/siteverify"
            )
    @Headers("Content-Type: application/x-www-form-urlencoded")
//    @Body("secret={secret}&response={response}")
    fun siteverify(
            body: String
    ): RecaptchaResponse
}

data class RecaptchaRequestBody(
        val secret: String,
        var response: String
)

*/
/*
{
  "success": true|false,
  "challenge_ts": timestamp,  // timestamp of the challenge load (ISO format yyyy-MM-dd'T'HH:mm:ssZZ)
  "hostname": string,         // the hostname of the site where the reCAPTCHA was solved
  "error-codes": [...]        // optional
}
 *//*

data class RecaptchaResponse(
        val success: Boolean,
        @JsonProperty("challenge_ts")
        val challengeTs: LocalDateTime?,
        val hostname: String?,
        @JsonProperty("error-codes")
        val errorCodes: List<String>?
)

class CoreFeignConfiguration {

    @Autowired
    private lateinit var messageConverters: ObjectFactory<HttpMessageConverters>


    @Bean
    @Primary
    @Scope(BeanDefinition.SCOPE_PROTOTYPE)
    fun feignFormEncoder(): FormEncoder {
        return FormEncoder(SpringEncoder(this.messageConverters))
    }
}*/
