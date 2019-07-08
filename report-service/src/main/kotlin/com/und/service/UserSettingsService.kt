package com.und.service

import com.und.repository.jpa.ClientSettingsRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.util.*


@Service
class UserSettingsService {


    @Autowired
    private lateinit var clientSettingsRepository: ClientSettingsRepository


    companion object {
        var logger = LoggerFactory.getLogger(UserSettingsService::class.java)
    }



    fun getTimeZoneByClientId(clientID: Long): ZoneId {
        val tz = clientID.let {
            clientSettingsRepository.findByClientID(clientID)?.timezone
        } ?: TimeZone.getDefault().id
        return ZoneId.of(tz)

    }


}

enum class ServiceProviderType(val desc: String) {
    EMAIL_SERVICE_PROVIDER("Email Service Provider"),
    SMS_SERVICE_PROVIDER("SMS Service Provider"),
    NOTIFICATION_SERVICE_PROVIDER("Notification Service Provider"),
    ANDROID_PUSH_SERVICE_PROVIDER("Android Push Service Provider"),
    WEB_PUSH_SERVICE_PROVIDER("Web Push Service Provider"),
    IOS_PUSH_SERVICE_PROVIDER("iOS Push Service Provider")

}

enum class KEYTYPE {
    ADMIN_LOGIN,
    EVENT_ANDROID,
    EVENT_IOS,
    EVENT_WEB
}


enum class Security {
    SSL, TLS, STARTTLS, NONE
}