package com.und.repository.jpa

import com.und.model.ClientSettings
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ClientSettingRepository:JpaRepository<ClientSettings,Long> {
    fun findByClientID(clientId:Long):Optional<ClientSettings>
}
