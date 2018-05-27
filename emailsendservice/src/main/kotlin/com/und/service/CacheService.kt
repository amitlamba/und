package com.und.service

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.util.*

@Service
class CacheService {

    @Cacheable(key = "#id", cacheNames = arrayOf("random"))
    fun cachingFunction(id: Int): String {
        val generator = Random(System.currentTimeMillis())
        return generator.nextDouble().toString()
    }
}