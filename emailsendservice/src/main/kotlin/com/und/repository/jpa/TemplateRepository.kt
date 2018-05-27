package com.und.repository.jpa

import com.und.model.jpa.Template
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface TemplateRepository : JpaRepository<Template, Long> {
    fun findByName(name: String): Optional<Template>
}