package com.und.repository.jpa

import com.und.model.jpa.SystemEmail
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SystemEmailRepository:JpaRepository<SystemEmail,Long> {
    fun findByEmailTemplateId(id:Long):SystemEmail?
}