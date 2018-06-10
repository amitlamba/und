package com.und.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate

@Configuration
class CaptchaConfig {
    @Bean
    fun clientHttpRequestFactory(): ClientHttpRequestFactory {
        val factory = SimpleClientHttpRequestFactory()
        factory.setConnectTimeout(3 * 1000)
        factory.setReadTimeout(7 * 1000)
        return factory
    }

    @Bean
    fun restTemplate(): RestOperations {
        return RestTemplate(clientHttpRequestFactory())
    }
}
