package com.und.web.model

import com.und.model.Status
import java.util.*

class ServiceProviderCredentials {
    var id: Long? = null
    var clientID: Long? = null
    var appuserID: Long? = null
    lateinit var serviceProviderType: String
    lateinit var serviceProvider: String
    lateinit var status: Status
    var credentialsMap: HashMap<String, String> = HashMap()
}