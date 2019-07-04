package com.und.service.segmentquerybuilder

import com.und.repository.jpa.ClientSettingsRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.util.*

@Service
class UserSettingsService {


    @Autowired
    private lateinit var clientSettingsRepository: ClientSettingsRepository

    fun getTimeZoneByClientId(clientID: Long): ZoneId {
        val tz = clientID.let {
            clientSettingsRepository.findByClientID(clientID)?.timezone
        } ?: TimeZone.getDefault().id
        return ZoneId.of(tz)

    }
}