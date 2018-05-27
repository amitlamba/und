package com.und.web.model

import com.fasterxml.jackson.annotation.JsonProperty
import javax.mail.internet.InternetAddress
import javax.validation.constraints.Email

data class EmailAddress(@JsonProperty("address") @Email val address: String, @JsonProperty("personal") val personal: String) {
    companion object {
        fun fromInternetAddress(internetAddress: InternetAddress): EmailAddress {
            return EmailAddress(internetAddress.address, internetAddress.personal)
        }
        fun toInternetAddress(emailAddress: EmailAddress): InternetAddress {
            return InternetAddress(emailAddress.address, emailAddress.personal)
        }
    }
}