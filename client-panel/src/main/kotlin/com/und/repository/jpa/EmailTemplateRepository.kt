package com.und.repository.jpa

import com.und.model.jpa.EmailTemplate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface EmailTemplateRepository : JpaRepository<EmailTemplate, Long> {
    fun existsByNameAndClientID(name:String, clientID: Long): Boolean
    fun findByNameAndClientID(name:String, clientID: Long): Optional<EmailTemplate>
    fun findByClientID(clientID: Long = 1): List<EmailTemplate>
    //why we would return a list template id is unique key so there is always either one or zero template
    fun findByIdAndClientID(id: Long, clientID: Long): Optional<EmailTemplate>
}