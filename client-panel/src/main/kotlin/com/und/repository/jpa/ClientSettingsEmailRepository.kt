package com.und.repository.jpa

import com.und.model.jpa.ClientSettingsEmail
import com.und.web.model.ClientEmailSettIdFromAddrSrp
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ClientSettingsEmailRepository : JpaRepository<ClientSettingsEmail, Long> {

    fun findByClientIdAndDeleted(clientId: Long, deleted: Boolean): List<ClientSettingsEmail>?

    fun findByClientIdAndVerified(clientId: Long,verified:Boolean):List<ClientSettingsEmail>?

    fun findByEmailAndClientIdAndDeleted(email: String, clientId: Long, deleted: Boolean): ClientSettingsEmail?

    fun existsByEmailAndClientIdAndDeleted(email: String, clientId: Long, deleted: Boolean): Boolean

    fun findByEmailAndClientId(mail:String,clientId: Long):ClientSettingsEmail

    fun existsByClientIdAndEmailAndVerified(clientId: Long, from: String, b: Boolean): Boolean

    fun findByClientIdAndEmailAndServiceProviderId(clientId: Long,email: String,srpid:Long):Optional<ClientSettingsEmail>

    fun findByClientIdAndId(clientId: Long,id:Long):Optional<ClientSettingsEmail>
}