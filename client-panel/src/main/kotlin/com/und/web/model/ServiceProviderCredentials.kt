package com.und.web.model

import com.und.model.Status
import java.util.*
import javax.validation.constraints.NotNull

class ServiceProviderCredentials {
    var id: Long? = null
    var clientID: Long? = null
    var appuserID: Long? = null

    @NotNull
    lateinit var serviceProviderType: String

    @NotNull
    lateinit var serviceProvider: String

    @NotNull
    var status: Status = Status.ACTIVE

    var credentialsMap: HashMap<String, String> = HashMap()
}