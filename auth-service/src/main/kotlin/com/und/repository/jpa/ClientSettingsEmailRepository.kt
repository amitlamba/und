package com.und.repository.jpa

import com.und.model.jpa.ClientSettingsEmail
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ClientSettingsEmailRepository :JpaRepository<ClientSettingsEmail,Long>{
}