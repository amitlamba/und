package com.und.repository.jpa

import com.und.model.jpa.Template
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate.now

@Repository
interface TemplateRepository : JpaRepository<Template, Long> {

    override fun count():Long

    @Query("Select count(*) from Template t where t.dateCreated=CURRENT_DATE")
    fun getNewTempletes():Long
}