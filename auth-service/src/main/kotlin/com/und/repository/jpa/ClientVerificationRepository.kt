package com.und.repository.jpa

import com.und.model.jpa.ClientVerification
import com.und.model.jpa.security.Client
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ClientVerificationRepository : JpaRepository<ClientVerification, Long> {
    fun findByEmailCodeAndClient(code: String, clientId: Long?): ClientVerification?
    //fun findByClientId(name: String): ClientVerification?
}