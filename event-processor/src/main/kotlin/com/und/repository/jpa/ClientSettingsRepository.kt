package com.und.repository.jpa

import com.und.model.jpa.ClientSettings
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*
import javax.transaction.Transactional

@Repository
interface ClientSettingsRepository : JpaRepository<ClientSettings, Int> {

    @Query(value = "Select sender_email_addresses from client_settings where client_id = :clientId", nativeQuery = true)
    fun findSenderEmailAddressesByClientId(clientId: Long): String?

    @Modifying
    @Query(value = "Update client_settings set sender_email_addresses = :senderEmailAddresses where client_id = :clientId", nativeQuery = true)
    fun saveSenderEmailAddresses(senderEmailAddresses: String, clientId: Long)

    fun findByClientID(clientId: Long): ClientSettings?

    fun findByIdAndClientID(id:Long, clientId: Long): ClientSettings?

    @Transactional
    @Modifying(flushAutomatically = true,clearAutomatically = true)
    @Query(value = "Update client_settings set authorized_urls = :authorizedUrls,android_app_ids= :andAppId,ios_app_ids= :iosAppId, timezone = :timezone where client_id = :clientId", nativeQuery = true)
    fun updateAccountSettings(@Param("authorizedUrls")authorizedUrls: String?,
                              @Param("andAppId")andAppId:String?,
                              @Param("iosAppId")iosAppId:String?,
                              @Param("timezone")timezone: String,
                              @Param("clientId")clientId: Long)
}