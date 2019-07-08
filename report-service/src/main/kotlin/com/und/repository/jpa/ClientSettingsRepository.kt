package com.und.repository.jpa

import com.und.model.jpa.ClientSettings
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import javax.transaction.Transactional

@Repository
interface ClientSettingsRepository : JpaRepository<ClientSettings, Int> {

    fun findByClientID(clientId: Long): ClientSettings?
}