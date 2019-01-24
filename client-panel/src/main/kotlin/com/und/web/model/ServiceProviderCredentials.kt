package com.und.web.model

import com.fasterxml.jackson.annotation.JsonGetter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSetter
import com.und.model.Status
import java.util.*
import javax.validation.constraints.NotNull

@JsonIgnoreProperties("default")
class ServiceProviderCredentials {
    var id: Long? = null
    var clientID: Long? = null
    var appuserID: Long? = null

    @NotNull
    lateinit var serviceProviderType: String

    @NotNull
    lateinit var serviceProvider: String

    @NotNull
    lateinit var name:String

    @NotNull
    var status: Status = Status.ACTIVE

    var credentialsMap: HashMap<String, String> = HashMap()

    @JsonProperty("isDefault")
    var isDefault:Boolean=false
//        @JsonGetter("isDefault")
//    get() =isDefault?:false
//        @JsonSetter("isDefault")
//    set(value) {isDefault=value}

}