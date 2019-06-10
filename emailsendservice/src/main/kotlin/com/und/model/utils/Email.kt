package com.und.model.utils

import com.und.common.utils.DateUtils
import com.und.model.mongo.EmailStatus
import com.und.model.mongo.EventUser
import java.util.*
import javax.mail.internet.InternetAddress

data class Email(
        var clientID: Long,
        var fromEmailAddress: InternetAddress? = null,
        var toEmailAddresses: Array<InternetAddress>,
        var ccEmailAddresses: Array<InternetAddress>? = null,
        var bccEmailAddresses: Array<InternetAddress>? = null,
        var replyToEmailAddresses: Array<InternetAddress>? = null,
        var emailSubject: String? = null,
        var emailBody: String? = null,
        var emailTemplateId: Long,
        var emailTemplateName: String,
        var data: MutableMap<String, Any> = mutableMapOf(),
        var eventUser: EventUser? = null,
        var campaignId:Long?=null,
        var creationTime: Date = DateUtils.nowInUTC(),
        var retries:Int = 0,
        var mongoNotificationId:String?=null,
        var clientEmailSettingId:Long? = null,
        var tmpltVisiability:Boolean=false,
        //var serviceProviderId:Long?=null
        var segmentId:Long?=null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Email

        if (clientID != other.clientID) return false
        if (fromEmailAddress != other.fromEmailAddress) return false
        if (!Arrays.equals(toEmailAddresses, other.toEmailAddresses)) return false
        if (!Arrays.equals(ccEmailAddresses, other.ccEmailAddresses)) return false
        if (!Arrays.equals(bccEmailAddresses, other.bccEmailAddresses)) return false
        if (!Arrays.equals(replyToEmailAddresses, other.replyToEmailAddresses)) return false
        if (emailSubject != other.emailSubject) return false
        if (emailBody != other.emailBody) return false
        if (emailTemplateId != other.emailTemplateId) return false
        if (emailTemplateName != other.emailTemplateName) return false
        if (data != other.data) return false
        if (eventUser != other.eventUser) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clientID.hashCode()
        result = 31 * result + (fromEmailAddress?.hashCode()?:0)
        result = 31 * result + Arrays.hashCode(toEmailAddresses)
        result = 31 * result + (ccEmailAddresses?.let { Arrays.hashCode(it) } ?: 0)
        result = 31 * result + (bccEmailAddresses?.let { Arrays.hashCode(it) } ?: 0)
        result = 31 * result + (replyToEmailAddresses?.let { Arrays.hashCode(it) } ?: 0)
        result = 31 * result + (emailSubject?.hashCode() ?: 0)
        result = 31 * result + (emailBody?.hashCode() ?: 0)
        result = 31 * result + emailTemplateId.hashCode()
        result = 31 * result + emailTemplateName.hashCode()
        result = 31 * result + data.hashCode()
        result = 31 * result + (eventUser?.hashCode() ?: 0)
        return result
    }
}

data class EmailUpdate(
        var clientID: Long,
        var mongoEmailId: String,
        val emailStatus: EmailStatus,
        val eventId: String? = null
)