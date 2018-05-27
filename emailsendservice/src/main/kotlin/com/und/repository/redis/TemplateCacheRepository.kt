package com.und.repository.redis

import com.und.model.redis.CachedTemplate
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TemplateCacheRepository : CrudRepository<CachedTemplate, String>{
}