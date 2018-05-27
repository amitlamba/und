package com.und.model

import javax.mail.internet.InternetAddress

data class Email(
        var clientID: Long,
        var fromEmailAddress: InternetAddress,
        var toEmailAddresses: Array<InternetAddress>,
        var ccEmailAddresses: Array<InternetAddress>? = null,
        var bccEmailAddresses: Array<InternetAddress>? = null,
        var replyToEmailAddresses: Array<InternetAddress>? = null,
        var emailSubject: String,
        var emailBody: String,
        var userID: String? = null
)