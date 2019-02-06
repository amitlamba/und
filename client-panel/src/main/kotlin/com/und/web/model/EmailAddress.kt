package com.und.web.model

import com.fasterxml.jackson.annotation.JsonProperty
import javax.mail.internet.InternetAddress
import javax.validation.constraints.Email

data class EmailAddress(@JsonProperty("address") @Email val address: String, @JsonProperty("personal") val personal: String, @JsonProperty("serviceProviderId") val serviceProviderId: Long) {

    var status: Boolean = false

    constructor(address: String, personal: String, serviceProviderId: Long, status: Boolean) : this(address, personal, serviceProviderId) {
        this.status = status
    }

    companion object {

    }
}