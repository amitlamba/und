package com.und.web.model.eventapi

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.und.eventapi.validation.*
import com.und.model.mongo.eventapi.AppField
import com.und.model.mongo.eventapi.LineItem
import java.time.*
import java.util.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

/**
 * Created by shiv on 21/07/17.
 */

//FIXME handle validation for other fields and different types, e.g. @see
open class Event {

    @Size(min = 2, max = 40, message = "{event.name.invalidSize}")
    lateinit var name: String

    var clientId: Long = -1L
    var identity: Identity = Identity()

//    @JsonDeserialize(using=CustomLongToLocalDateTimeDeserializer::class)
//    var creationTime: LocalDateTime = LocalDateTime.now(ZoneId.of("UTC"))
    @NotNull
    var creationTime:Long=System.currentTimeMillis()

    @Pattern(regexp = "(([0-9]|[1][0-9]{1,2}|2[0-4][0-9]|25[0-5])[.]){3}([0-9]|[1][0-9]{1,2}|2[0-4][0-9]|25[0-5])", message = "{event.ip.invalid}")
    var ipAddress: String? = null

    @Size(min = 2, max = 40, message = "{event.city.invalidSize}")
    @Pattern(regexp = "[A-Za-z][a-zA-Z\\s]*", message = "{event.city.invalid}")
    var city: String? = null

    @Size(min = 2, max = 40, message = "{event.state.invalidSize}")
    @Pattern(regexp = "[A-Za-z][a-zA-Z\\s]*", message = "{event.state.invalid}")
    var state: String? = null

    @Size(min = 2, max = 40, message = "{event.country.invalidSize}")
    @Pattern(regexp = "[A-Za-z][a-zA-Z\\s]*", message = "{event.country.invalid}")
    var country: String? = null

    @ValidateLatitude(message = "{event.latitude.invalid}")
    var latitude: String? = null

    @ValidateLongitude(message = "{event.longitude.invalid}")
    var longitude: String? = null

    var agentString: String? = null
    var userIdentified: Boolean = false
    var lineItem: MutableList<LineItem> = mutableListOf()
    var attributes: HashMap<String, Any> = hashMapOf()

    var appField: AppField? = null

    //@get:JsonIgnore
    var timeZone: String? = null

    var notificationId: String? = null

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

class date : Event() {
    //TODO
}

data class EventMessage(
        var eventId: String = "",
        var clientId: Long,
        var userId: String?,
        var name: String,
        var creationTime: Date,
        var userIdentified:Boolean
)

/* {
    var eventUser: EventUser = EventUser()
}
*/

class CustomLongToLocalDateTimeDeserializer:StdDeserializer<LocalDateTime>{

    constructor():this(null)

    constructor(vc: Class<*>?) : super(vc)


    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): LocalDateTime {

        p?.let {
            return LocalDateTime.from(Instant.ofEpochMilli(p.text.toLong()).atZone(ZoneId.of("UTC")))
        }
        return LocalDateTime.now(ZoneId.of("UTC"))
    }
}


