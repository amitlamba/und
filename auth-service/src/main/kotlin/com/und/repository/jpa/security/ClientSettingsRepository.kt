package com.und.repository.jpa.security

import com.und.model.jpa.security.ClientSettings
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Created by shiv on 21/07/17.
 */
@Repository
interface ClientSettingsRepository : JpaRepository<ClientSettings, Long> {


    fun findByclientID(clientId: Long): Optional<ClientSettings>


}
