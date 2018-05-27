package com.und.repository.jpa

import com.und.model.jpa.CampaignAuditLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CampaignAuditLogRepository : JpaRepository<CampaignAuditLog, Long>