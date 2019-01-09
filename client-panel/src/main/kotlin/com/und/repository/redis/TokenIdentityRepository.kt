package com.und.repository.redis

import com.und.model.redis.TokenIdentity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TokenIdentityRepository:CrudRepository<TokenIdentity,String> {
}