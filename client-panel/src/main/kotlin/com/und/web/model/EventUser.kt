package com.und.web.model

import java.time.LocalDateTime
import java.util.*

class EventUser {

    var identity: Identity = Identity()

    var email: String? = null

    var clientUserId: String? = null //this is id of the user client has provided

    var undId: String? = null

    var fbId: String? = null

    var googleId: String? = null

    //TODO Use custom validators here

    var mobile: String? = null

    var firstName: String? = null

    var lastName: String? = null

    var gender: String? = null

    var dob: String? = null

    var country: String? = null

    var city: String? = null

    var address: String? = null

    var countryCode: String? = null

    var clientId: Int = -1 //client id , user is associated with, this can come from collection

    var additionalInfo: HashMap<String, Any> = hashMapOf()

    //FIXME creation date can't keep changing
    var creationDate: LocalDateTime = LocalDateTime.now()


}


data class Identity(
        //unique id assigned to a device, should always remain fixed, create new if not found
        var deviceId: String = "",
        //if userId is not found assign a new session id, handle change if user login changes, logouts etc
        var sessionId: String = "",
        // id of event user, this id is assigned when a user profile is identified.
        var userId: String? = null,
        var clientId: Int? = -1
)

