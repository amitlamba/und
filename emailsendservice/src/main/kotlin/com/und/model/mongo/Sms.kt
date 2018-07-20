package com.und.model.mongo

import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import javax.persistence.Id

@Document(collection = "#{tenantProvider.getTenant()}_sms")
data class Sms(
        var clientID: Long,
        var fromSmsAddress: String? = null,
        var toSmsAddresses: Array<String>,
        @Transient
        var smsBody:String,
        var smsTemplateId: Long? = null,
        var userID: String? = null,
        var campaignID: Long? = null,
        @Id
        var id: String? = null, //Mongo Auto-generated Document id
        var smsProviderMessageID: String? = null,
        var smsServiceProvider: String? = null,
        var smsStatus: SmsStatus,
        var statusUpdates: MutableList<SmsStatusUpdate> = mutableListOf()
)

enum class SmsStatus(val order: Int) {
    NOT_SENT(1),
    SENT(2),
    READ(3),
    CTA_PERFORMED(4);
}

data class SmsStatusUpdate (
        val date: LocalDateTime,
        val smsStatus: SmsStatus,
        val clickTrackEventId: String? = null
)