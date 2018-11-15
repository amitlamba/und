package com.und.model.mongo

import com.und.common.utils.DateUtils
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.*
import javax.persistence.Id

@Document(collection = "#{tenantProvider.getTenant()}_fcmMessage")
data class AnalyticFcmMessage(
        @Id
        var id: String? = null,
        var clientId: Long,
        var campaignId: Long,
        var status: FcmMessageStatus,
        var statusUpdates: MutableList<FcmMessageUpdates> = mutableListOf<FcmMessageUpdates>(),
        var templateId: Long,
        var userId: String?=null,
        var serviceProvider: String,
        var creationTime: Date = DateUtils.nowInUTC()
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
        val fcmStatus: FcmMessageStatus,
        val clickTrackEventId: String? = null
)