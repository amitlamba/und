package com.und.repository.jpa

import com.und.model.jpa.ClientSettings
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ClientSettingsRepository : JpaRepository<ClientSettings, Int> {

    @Query(value = "Select sender_email_addresses from client_settings where client_id = :clientId", nativeQuery = true)
    fun findSenderEmailAddressesByClientId(clientId: Long): String?

    @Modifying
    @Query(value = "Update client_settings set sender_email_addresses = :senderEmailAddresses where client_id = :clientId", nativeQuery = true)
    fun saveSenderEmailAddresses(senderEmailAddresses: String, clientId: Long)

    fun findByClientID(clientId: Long): ClientSettings?
}