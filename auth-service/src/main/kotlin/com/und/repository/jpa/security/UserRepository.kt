package com.und.repository.jpa.security

import org.springframework.data.jpa.repository.JpaRepository
import com.und.model.jpa.security.User
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * Created by shiv on 21/07/17.
 */
@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): User?

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.androidKey = :jwt WHERE u.username = :username")
    fun updateJwtOfEventUserAndroid(@Param("jwt") jwt: String, @Param("username") username: String):Int

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.key = :jwt WHERE u.username = :username")
    fun updateJwtOfEventUser(@Param("jwt") jwt: String, @Param("username") username: String):Int

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.iosKey = :jwt WHERE u.username = :username")
    fun updateJwtOfEventUserIos(@Param("jwt") jwt: String, @Param("username") username: String):Int

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.password = :password WHERE u.username = :username")
    fun resetPassword(@Param("password") password: String, @Param("username") username: String)
}
