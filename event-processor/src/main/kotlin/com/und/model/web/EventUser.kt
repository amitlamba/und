package com.und.model.web

import java.util.*

class EventUser {

    var identity: Identity = Identity()

    var email: String? = null

    var uid: String? = null //this is id of the user client has provided

    var undId: String? = null

    var fbId: String? = null

    var googleId: String? = null

    var androidFcmToken:String?=null
    var iosFcmToken:String?=null
    var webFcmToken:String?=null
    //TODO Use custom validators here

    var mobile: String? = null

    var firstName: String? = null

    var lastName: String? = null

    var gender: String? = null

    var dob: String? = null

    var country: String? = null

    var city: String? = null

    var state: String? = null

    var address: String? = null

    var countryCode: String? = null

    var clientId: Int = -1 //client id , user is associated with, this can come from collection

    var additionalInfo: HashMap<String, Any> = hashMapOf()

    //FIXME creation date can't keep changing
//    @JsonDeserialize(using=CustomLongToLocalDateTimeDeserializer::class)
//    var creationDate: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))
    var creationDate:Long=System.currentTimeMillis()


}




