package com.und.repository.jpa

import com.und.model.jpa.security.Client
import com.und.model.jpa.security.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ClientRepository : JpaRepository<Client, Long> , ClientRepositoryCustom{
    fun findByName(name: String): Client?
    fun findByEmail(email: String): Client?
}
