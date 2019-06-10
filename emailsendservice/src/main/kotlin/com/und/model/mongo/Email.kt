package com.und.model.mongo

import com.und.common.utils.DateUtils
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.*
import javax.mail.internet.InternetAddress

//@Document(collection = "#{tenantProvider.getTenant()}_email")
data class Email(
        @Id
        var id: String? = null, //Mongo Auto-generated Document id
        var clientID: Long,
        var toEmailAddresses: Array<InternetAddress>,
        var emailSubject: String,
        var campaignId: Long,
        var status: EmailStatus,
        var statusUpdates: MutableList<EmailStatusUpdate> = mutableListOf(),
        var emailTemplateId: Long,
        var fromEmailAddress: InternetAddress? = null,
        var ccEmailAddresses: Array<InternetAddress>? = null,
        var bccEmailAddresses: Array<InternetAddress>? = null,
        var replyToEmailAddresses: Array<InternetAddress>? = null,
        var userID: String? = null,
        var emailProviderMessageID: String? = null,
        var emailServiceProvider: String? = null,
        var creationTime: Date = DateUtils.nowInUTC(),
        var segmentId:Long?=null
//FIXME add creation date
) {
    @Transient
    var emailBody: String=""
}

data class EmailStatusUpdate (
        val date: LocalDateTime,
        val status: EmailStatus,
        val clickTrackEventId: String? = null
)

enum class EmailStatus(val order: Int) {
    NOT_SENT(1),
    SENT(2),
    READ(3),
    CTA_PERFORMED(4),
    ERROR(5)
}