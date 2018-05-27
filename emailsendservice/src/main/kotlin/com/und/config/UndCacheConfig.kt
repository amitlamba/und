package com.und.config

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories
import java.time.Duration


@Configuration
@EnableCaching
@EnableRedisRepositories
class UndCacheConfig {

    @Bean
    fun connectionFactory(): RedisConnectionFactory {
        return JedisConnectionFactory()
    }

    @Bean
    fun redisTemplate(): RedisTemplate<*, *> {

        val template =  RedisTemplate<ByteArray, ByteArray>()
        template.connectionFactory = connectionFactory()
        return template
    }

    @Bean
    fun cacheManager(redisTemplate: RedisTemplate<*, *>): CacheManager {
        var redisCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(1))
        val cacheManager = RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(connectionFactory())
                .withInitialCacheConfigurations(linkedMapOf(Pair("defaultCacheConfig",redisCacheConfig)))
                .build()

        return cacheManager
    }

}