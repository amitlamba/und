package com.und.repository.redis

import com.und.model.redis.UserCache
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserCacheRepository:CrudRepository<UserCache,String> {
}