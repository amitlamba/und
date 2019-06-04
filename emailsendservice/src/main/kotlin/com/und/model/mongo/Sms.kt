package com.und.model.mongo

import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import javax.persistence.Id

//@Document(collection = "#{tenantProvider.getTenant()}_sms")
data class Sms(
        //here we are not saving all details why add extra properties
        var clientID: Long,
        var fromSmsAddress: String? = null,
        var toSmsAddresses: String?,
        @Transient
        var smsBody: String?,
        var smsTemplateId: Long? = null,

        var userID: String? = null,
        var campaignId: Long? = null,
        @Id
        var id: String? = null, //Mongo Auto-generated Document id
        var smsProviderMessageID: String? = null,
        var smsServiceProvider: String? = null,
        var status: SmsStatus,
        var statusUpdates: MutableList<SmsStatusUpdate> = mutableListOf(),
        var segmentId:Long?=null
)

enum class SmsStatus(val order: Int) {
    NOT_SENT(1),
    SENT(2),
    READ(3),
    CTA_PERFORMED(4),
    ERROR(5)
}

data class SmsStatusUpdate(
        val date: LocalDateTime,
        val status: SmsStatus,
        val clickTrackEventId: String? = null,
        var message: String? = null
)