package com.und.config

import com.und.config.EventStream
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.context.annotation.Configuration


@EnableBinding(EventStream::class)
class JacksonConfig {

/*    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        val mapper = ObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true)

        return mapper
    }*/
}