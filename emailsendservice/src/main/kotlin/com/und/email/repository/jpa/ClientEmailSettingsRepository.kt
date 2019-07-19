package com.und.email.repository.jpa

import com.und.model.jpa.ClientSettingsEmail
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ClientEmailSettingsRepository:JpaRepository<ClientSettingsEmail,Long> {

    fun findByClientIdAndEmailAndServiceProviderId(clientId:Long,email:String,sp:Long):Optional<ClientSettingsEmail>
}