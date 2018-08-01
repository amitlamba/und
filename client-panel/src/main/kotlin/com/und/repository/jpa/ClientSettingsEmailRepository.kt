package com.und.repository.jpa

import com.und.model.jpa.ClientSettingsEmail
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ClientSettingsEmailRepository : JpaRepository<ClientSettingsEmail, Int> {

    fun findByClientIdAndDeleted(clientId: Long, deleted: Boolean): List<ClientSettingsEmail>?

    fun findByEmailAndClientIdAndDeleted(email: String, clientId: Long, deleted: Boolean): ClientSettingsEmail?

    fun existsByEmailAndClientIdAndDeleted(email: String, clientId: Long, deleted: Boolean): Boolean

    fun findByEmailAndClientId(mail:String,clientId: Long):ClientSettingsEmail
}