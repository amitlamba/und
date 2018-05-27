package com.und.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration


@Configuration
@EnableRedisRepositories
class RedisConfig {

    @Value("\${spring.redis.host}")
    lateinit var host: String

    @Value("\${spring.redis.port}")
    private var port: String = "6379"

    @Bean
    fun connectionFactory(): RedisConnectionFactory {
        val redisConfig = RedisStandaloneConfiguration(host, port.toInt())
        return JedisConnectionFactory(redisConfig)
    }

    @Bean
    fun redisTemplate(): RedisTemplate<*, *> {

        val template = RedisTemplate<ByteArray, ByteArray>()
        template.connectionFactory = connectionFactory()
        return template
    }
}