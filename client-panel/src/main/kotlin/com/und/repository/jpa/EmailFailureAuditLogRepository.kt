package com.und.repository.jpa

import com.und.model.jpa.EmailFailureAuditLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EmailFailureAuditLogRepository : JpaRepository<EmailFailureAuditLog, Long> {
    //fun findTopBycampaignIdAndClientIDOrderByIdDesc(campaignId: Long, clientId: Long): Optional<CampaignAuditLog>
}