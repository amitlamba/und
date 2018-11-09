package com.und.repository.jpa

import com.und.model.jpa.FcmFailureAuditLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FcmFailureAuditLogRepository:JpaRepository<FcmFailureAuditLog,Long>{

}