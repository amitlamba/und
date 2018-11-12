package com.und.model

import com.und.model.mongo.eventapi.EventUser
import javax.mail.internet.InternetAddress


data class NotificationEmail(
        var clientID: Long,
        var fromEmailAddress: InternetAddress? = null,
        var toEmailAddresses: Array<InternetAddress>,
        var emailSubject: String? = null,
        var emailBody: String? = null,
        var notificationTemplateId: Long,
        var notificationTemplateName: String,
        var data: MutableMap<String, Any> = mutableMapOf(),
        var eventUser: EventUser? = null
)