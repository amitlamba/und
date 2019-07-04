package com.und.model

import java.time.LocalDateTime

open class NotificationRead(
        var mongoId:String,
        var clientId:Long,
        var type:String
)

enum class FcmMessageStatus(var order: Int) {
    NOT_SENT(1),
    SENT(2),
    READ(3),
    CTA_PERFORMED(4),
    ERROR(5)
}

data class FcmMessageUpdates(
        val date: LocalDateTime,
        val status: FcmMessageStatus,
        val clickTrackEventId: String? = null
)