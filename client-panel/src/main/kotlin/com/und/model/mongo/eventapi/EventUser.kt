package com.und.model.mongo.eventapi

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.HashMap

@Document(collection = "#{tenantProvider.getTenant()}_eventUser")
class EventUser {
        @Id
        var id: String? = null
        var clientId: String? = null //client id , user is associated with, this can come from collection
        var clientUserId: String? = null//this is id of the user client has provided
        var socialId: SocialId = SocialId()
        var standardInfo: StandardInfo = StandardInfo()
        var additionalInfo: HashMap<String, Any> = hashMapOf()
        //FIXME creation date can't keep changing
        var creationDate: Long = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()

        fun copyNonNull(eventUser: EventUser): EventUser {

                val copyEventUser = EventUser()
                copyEventUser.additionalInfo.putAll(additionalInfo)
                copyEventUser.additionalInfo.putAll(eventUser.additionalInfo)
                copyEventUser.clientUserId = unchanged(eventUser.clientUserId, clientUserId)
                copyEventUser.socialId = socialId.copyNonNull(eventUser.socialId)
                copyEventUser.standardInfo = standardInfo.copyNonNull(eventUser.standardInfo)
                return copyEventUser
        }

}

data class SocialId(
        var fbId: String? = null,
        var googleId: String? = null,
        var mobile: String? = null,
        var email: String? = null
) {

        fun copyNonNull(socialId: SocialId): SocialId {
                val copySocialId = socialId.copy()
                copySocialId.fbId = unchanged(socialId.fbId, fbId)
                copySocialId.googleId = unchanged(socialId.googleId, googleId)
                copySocialId.mobile = unchanged(socialId.mobile, mobile)
                copySocialId.email = unchanged(socialId.email, email)
                return copySocialId
        }
}

data class StandardInfo(
        var firstName: String? = null,
        var lastName: String? = null,
        var gender: String? = null,
        var dob: String? = null,
        var country: String? = null,
        var countryCode: String? = null
) {
        fun copyNonNull(standardInfo: StandardInfo): StandardInfo {
                val copyInfo = standardInfo.copy()
                copyInfo.firstName = unchanged(standardInfo.firstName, firstName)
                copyInfo.lastName = unchanged(standardInfo.lastName, lastName)
                copyInfo.gender = unchanged(standardInfo.gender, gender)
                copyInfo.dob = unchanged(standardInfo.dob, dob)
                copyInfo.country = unchanged(standardInfo.country, country)
                copyInfo.countryCode = unchanged(standardInfo.countryCode, countryCode)
                return copyInfo
        }
}

fun unchanged(new: String?, old: String?): String? = when {
        new == old -> old
        old == null -> new
        new == null -> old
        else -> new
}

