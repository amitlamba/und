package com.und.web.model

import com.und.model.mongo.Communication
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.validation.constraints.Email
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

class EventUser:EventUserMinimal(){

    @Email
    var email: String? = null

    var uid: String? = null //this is id of the user client has provided


    @Email
    var fbId: String? = null

    @Email
    var googleId: String? = null



    @Pattern(regexp = "(\\d{4})[-](0?[1-9]|1[012])[-](0?[1-9]|[12][0-9]|3[01])")
    var dob: String? = null


    @Size(min = 3, max = 50)
    @Pattern(regexp = "[A-Za-z][A-Za-z\\s]*")
    var city: String? = null

    @Size(min = 3, max = 255)
    var address: String? = null

    @Pattern(regexp = "\\+[0-9]{1,3}")
    var countryCode: String? = null

    var clientId: Int = -1 //client id , user is associated with, this can come from collection

    var additionalInfo: HashMap<String, Any> = hashMapOf()

    //FIXME creation date can't keep changing
    var creationDate: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))

    var communication: Communication = Communication()

    var testUser: Boolean = false

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

