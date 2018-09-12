package com.und.model

import com.und.model.mongo.eventapi.EventUser
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
        var eventUser: EventUser? = null
)