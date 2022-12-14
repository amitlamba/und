package com.und.model.mongo

import com.und.common.utils.DateUtils
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

class EventUser {
    var id: String? = null
    var clientId: Int = -1
    var identity: Identity = Identity()
    var standardInfo: StandardInfo = StandardInfo()
    val additionalInfo: HashMap<String, Any> = hashMapOf()
    var creationTime: Date = DateUtils.nowInUTC()
}


class Identity {
    var email: String? = null
    var uid: String? = null
    var undId: String? = null
    var fbId: String? = null
    var googleId: String? = null
    var mobile: String? = null

}

data class CommunicationDetails(val value: String, var dnd: Boolean = false)
class Communication {
    var email: CommunicationDetails? = null
    var mobile: CommunicationDetails? = null
}

class StandardInfo {
    var firstname: String? = null
    var lastname: String? = null
    var gender: String? = null
    var dob: String? = null
    var languages: MutableList<String> = mutableListOf()
    var country: String? = null
    var City: String? = null
    var Address: String? = null
}