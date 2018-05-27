package com.und.web.model.eventapi

import com.und.eventapi.validation.*
import com.und.model.mongo.eventapi.LineItem
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

/**
 * Created by shiv on 21/07/17.
 */

//FIXME handle validation for other fields and different types, e.g. @see
@ValidateDate(message="{event.date.invalid}")
open class Event {

    @Size(min=2,max=40,message="{event.name.invalidSize}")
    lateinit var name: String

    var clientId: Int = -1
    var identity: Identity = Identity()
    var creationTime: LocalDateTime = LocalDateTime.now()

    @Pattern(regexp="(([0-9]|[1][0-9]{1,2}|2[0-4][0-9]|25[0-5])[.]){3}([0-9]|[1][0-9]{1,2}|2[0-4][0-9]|25[0-5])",message="{event.ip.invalid}")
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

    @ValidateLatitude(message="{event.latitude.invalid}")
    var latitude: String? = null

    @ValidateLongitude(message="{event.longitude.invalid}")
    var longitude: String? = null

    var agentString: String? = null
    var userIdentified: Boolean = false
    var lineItem: MutableList<LineItem> = mutableListOf()
    var attributes: HashMap<String, Any> = hashMapOf()

    @ValidateDateFormat(message="{event.startDate.invalid}")
    private var startDate: LocalDate? = LocalDate.now()

    @ValidateDateFormat(message="{event.endDate.invalid}")
    private var endDate: LocalDate? = LocalDate.of(2020, 1, 1)

    fun getDateStart(): LocalDate? {
        return startDate
    }

    fun getDateEnd(): LocalDate? {
        return endDate
    }

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

/* {
    var eventUser: EventUser = EventUser()
}
*/


