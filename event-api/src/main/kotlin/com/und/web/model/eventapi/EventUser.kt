package com.und.web.model.eventapi

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*
import javax.validation.constraints.Email
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

class EventUser {

    var identity: Identity = Identity()

    @Email(message = "{eventUser.email.invalid}")
    var email: String? = null

    var uid: String? = null //this is id of the user client has provided

    var undId: String? = null

    @Email(message = "{eventUser.fbId.invalid}")
    var fbId: String? = null

    @Email(message = "{eventUser.googleId.invalid}")
    var googleId: String? = null

    var androidFcmToken:String?=null
    var iosFcmToken:String?=null
    var webFcmToken:String?=null
    //TODO Use custom validators here

    @Size(min = 10, max =15 , message = "{eventUser.mobile.invalidSize}")
    @Pattern(regexp = "[0-9]*", message = "{eventUser.mobile.invalidDigits}")
    var mobile: String? = null

    @Size(min = 2, max = 30, message = "{eventUser.firstName.invalidSize}")
    @Pattern(regexp = "[A-Za-z][a-zA-Z\\s]*", message = "{eventUser.firstName.invalid}")
    var firstName: String? = null

    @Size(min = 2, max = 30, message = "{eventUser.lastName.invalidSize}")
    @Pattern(regexp = "[A-Za-z][a-zA-Z\\s]*", message = "{eventUser.lastName.invalid}")
    var lastName: String? = null

    @Pattern(regexp = "[A-Za-z]+", message = "{eventUser.gender.invalidCharacters}")
    var gender: String? = null

    @Pattern(regexp = "(\\d{4})[-](0?[1-9]|1[012])[-](0?[1-9]|[12][0-9]|3[01])", message = "{eventUser.dob.invalid}")
    var dob: String? = null

    @Size(min = 2, max = 40, message = "{eventUser.country.invalidSize}")
    @Pattern(regexp = "[A-Za-z][a-zA-Z\\s]*", message = "{eventUser.country.invalid}")
    var country: String? = null

    @Size(min = 2, max = 40, message = "{eventUser.city.invalidSize}")
    @Pattern(regexp = "[A-Za-z][a-zA-Z\\s]*", message = "{eventUser.city.invalid}")
    var city: String? = null

    var address: String? = null

    @Pattern(regexp = "^(\\+?\\d{1,3})", message = "{eventUser.countryCode.invalid}")
    var countryCode: String? = null

    var clientId: Int = -1 //client id , user is associated with, this can come from collection

    var additionalInfo: HashMap<String, Any> = hashMapOf()

    //FIXME creation date can't keep changing
    @JsonDeserialize(using=CustomLongToLocalDateTimeDeserializer::class)
    var creationDate: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))


}




