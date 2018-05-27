package com.und.repository.jpa

import com.und.model.jpa.SmsTemplate
import com.und.model.Status
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SmsTemplateRepository: JpaRepository<SmsTemplate, Long> {
    fun findByClientIDAndStatus(clientID: Long = 1, status: Status = Status.ACTIVE): List<SmsTemplate>
    fun findByIdAndClientIDAndStatus(id: Long, clientID: Long, status: Status = Status.ACTIVE): SmsTemplate
}