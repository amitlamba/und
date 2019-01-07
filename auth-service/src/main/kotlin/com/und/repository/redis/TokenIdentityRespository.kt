package com.und.repository.redis

import com.und.model.redis.security.TokenIdentity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface TokenIdentityRespository :CrudRepository<TokenIdentity,String>{
}