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

    @Query("Select u from User u WHERE u.client = :clientId and u.id = :id and u.userType = :userType")
    fun findSystemUser(@Param("clientId") clientId: Long =1L, @Param("id") id: Long=1L,  @Param("userType") userType: Int=4):User

}
