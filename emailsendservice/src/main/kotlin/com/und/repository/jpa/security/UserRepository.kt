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

    @Modifying(clearAutomatically = true)
    @Query("Select User u WHERE u.client = :clientId and u.id = :id and u.userTpe=:userType")
    fun findSystemUser(@Param("clientId") clientId: Long =1, @Param("id") id: Long=1,  @Param("userType") userType: Int=4):User

}
