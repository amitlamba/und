package com.und.repository.jpa

import com.und.model.jpa.EmailTemplate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface EmailTemplateRepository : JpaRepository<EmailTemplate, Long> {
    fun findByIdAndClientID(id: Long, clientID: Long): Optional<EmailTemplate>
}