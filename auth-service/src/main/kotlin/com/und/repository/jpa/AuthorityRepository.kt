package com.und.repository.jpa

import com.und.model.jpa.security.Authority
import com.und.model.jpa.security.AuthorityName
import com.und.model.jpa.security.Client
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AuthorityRepository : JpaRepository<Authority, Long> {
    fun findByName(name: AuthorityName): Authority?
}