package com.und.sms.repository.mongo

import com.und.model.mongo.Sms
import com.und.model.mongo.SmsStatus
import org.springframework.stereotype.Repository

@Repository
interface SmsSentCustomRepository {

    fun saveSms(sms: Sms, clientId: Long)

    fun updateStatus(smsId: String, status: SmsStatus, clientId: Long, clickTrackEventId: String?, message: String)
}