package com.und.service.security

import com.und.repository.jpa.security.UserRepository
import com.und.repository.redis.UserCacheRepository
import com.und.model.redis.security.UserCache
import com.und.repository.jpa.security.ClientSettingsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class JWTKeyService {

    @Autowired
    lateinit var userCacheRepository: UserCacheRepository

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var clientSettingsRepository: ClientSettingsRepository


    fun updateJwt(jwt: UserCache): UserCache {
        //TODO fix updating only what is required
        return userCacheRepository.save(jwt)

    }


    fun getKeyIfExists(userId: Long): UserCache {
        val cacheKey = generateIdKey(userId)
        val jwtOption = userCacheRepository.findById(cacheKey)
        return if (!jwtOption.isPresent) {
            val jwtKeys = UserCache(secret = "", userId = generateIdKey(userId))
            val user = userRepository.findById(userId)
            if(user.isPresent) {
                with(jwtKeys) {
                    val clientId = user.get().client?.id?:-1
                    this.clientId = "${clientId}"
                    val clientSettings = clientSettingsRepository.findByclientID(clientId)
                    if(clientSettings.isPresent) {
                        this.timeZoneId = clientSettings.get().timezone
                    }

                    this.userId = generateIdKey(userId)
                    this.secret = user.get().clientSecret
                    this.loginKey = user.get().key
                    this.androidKey=user.get().androidKey
                    this.iosKey=user.get().iosKey
                    this.username = user.get().username
                    this.password = user.get().password
                    this.email = user.get().email
                    this.identified=false
                }
            }
            save(jwtKeys)
            jwtKeys

        } else {
            jwtOption.get()
        }


    }

    fun save(jwt: UserCache) {
        userCacheRepository.save(jwt)
    }

    private fun generateIdKey(userId: Long): String = "$userId"
}


