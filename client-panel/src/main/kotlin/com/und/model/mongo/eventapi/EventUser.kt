package com.und.model.mongo.eventapi

import com.und.common.utils.DateUtils
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

@Document(collection = "#{tenantProvider.getTenant()}_eventUser")
class EventUser {
    @field: Id
    var id: String? = null
    var clientId: Int = -1
    var identity: Identity = Identity()
    var standardInfo: StandardInfo = StandardInfo()
    val additionalInfo: HashMap<String, Any> = hashMapOf()
    var creationTime: Date = DateUtils.nowInUTC()
    var communication: Communication = Communication()
    var testUser:Boolean=false
}


class Identity {
    var email: String? = null
    var uid: String? = null
    var undId: String? = null
    var fbId: String? = null
    var googleId: String? = null
    var mobile: String? = null
    var androidFcmToken:String?=null
    var iosFcmToken:String?=null
    var webFcmToken:String?=null

}

data class CommunicationDetails(val value: String?, var dnd: Boolean = false)
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
    var city: String? = null
    var address: String? = null
    var countryCode: String? = null
}