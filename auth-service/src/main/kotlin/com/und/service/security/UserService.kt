package com.und.service.security

import com.und.common.utils.usernameFromEmailAndType
import com.und.repository.jpa.security.UserRepository
import com.und.model.jpa.security.UndUserDetails
import com.und.model.jpa.security.User
import com.und.model.redis.security.UserCache
import com.und.security.utils.AuthenticationUtils
import com.und.security.utils.KEYTYPE
import com.und.security.utils.RestTokenUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
@Transactional
class UserService {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit  var restTokenUtil: RestTokenUtil

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    fun findByUsername(username: String): User? {
        return userRepository.findByUsername(username)

    }

    fun updateJwtOfEventUser( adminUser: UndUserDetails,type: KEYTYPE): UserCache {
        //FIXME usernameFromEmailAndType method need fix and not required here
        val username = usernameFromEmailAndType(adminUser.username, AuthenticationUtils.USER_TYPE_EVENT)
        val jwt = generateJwtLogin(username,type)
        when(type){
            KEYTYPE.EVENT_ANDROID -> userRepository.updateJwtOfEventUserAndroid(jwt.androidKey?:"", username)
            KEYTYPE.EVENT_IOS -> userRepository.updateJwtOfEventUserIos(jwt.iosKey?:"", username)
            KEYTYPE.EVENT_WEB -> userRepository.updateJwtOfEventUser(jwt.loginKey?:"", username)
        }
        restTokenUtil.updateJwt(jwt)
        return jwt

    }


    fun retrieveJwtOfEventUser( adminUser: UndUserDetails,type:KEYTYPE): UserCache {
        //FIXME usernameFromEmailAndType method need fix and not required here
        val username = usernameFromEmailAndType(adminUser.username, AuthenticationUtils.USER_TYPE_EVENT)
        val jwt = retrieveJwtLogin(username)
        when(type){
            KEYTYPE.EVENT_ANDROID -> return if(jwt?.androidKey != null) jwt else updateJwtOfEventUser(adminUser,type)
            KEYTYPE.EVENT_IOS -> return if(jwt?.iosKey != null) jwt else updateJwtOfEventUser(adminUser,type)
            KEYTYPE.EVENT_WEB -> return if(jwt?.loginKey != null) jwt else updateJwtOfEventUser(adminUser,type)
        }
        return UserCache()
    }

    fun resetPassword(userDetails: UndUserDetails, password:String) {
        fun resetKeys(jwtToken: UserCache) {

            jwtToken.pswrdRstKey = null
            jwtToken.loginKey = null
            restTokenUtil.updateJwt(jwtToken)
        }

        val userId = userDetails.id
        if(userId!=null) {
            userRepository.resetPassword(passwordEncoder.encode(password),
                    userDetails.username)
            val jwtToken = restTokenUtil.getJwtIfExists(userId)
            resetKeys(jwtToken)
        }
    }

    fun generateJwtForForgotPassword(email: String): UserCache {
        return generateJwtLogin(email, KEYTYPE.PASSWORD_RESET)
    }

    private fun generateJwtLogin(username: String): UserCache {
        return generateJwtLogin(username, KEYTYPE.ADMIN_LOGIN)
    }

    private fun generateJwtLogin(username: String, keytype: KEYTYPE): UserCache {
        // Reload password post-security so we can generate token
        val user = findByUsername(username)
        return if (user != null) {
            restTokenUtil.generateJwtByUser(user, keytype)
        } else UserCache()
    }

    private fun retrieveJwtLogin(username: String): UserCache? {
        // Reload password post-security so we can generate token
        val user = findByUsername(username)
        return if (user != null) {
            restTokenUtil.retrieveJwtByUser(user)
        } else UserCache()
    }
}