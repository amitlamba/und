package com.und.repository.jpa

import com.und.model.jpa.SmsTemplate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*
@Repository
interface SmsTemplateRepository :JpaRepository<SmsTemplate,Long>{

    fun findByIdAndClientID(id:Long,clientId:Long):SmsTemplate?
}