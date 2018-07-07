package com.und.service

import com.und.repository.jpa.ClientSettingsRepository
import com.und.repository.mongo.ReportsServiceRepository
import com.und.security.utils.AuthenticationUtils
import com.und.web.model.EventCount
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

@Service
class ReportsService {
    @Autowired
    private lateinit var reportServiceRepository:ReportsServiceRepository

    @Autowired
    private lateinit var clientSettingsRepository: ClientSettingsRepository

    fun getEventsCount(fromDate: String, toDate: String): List<EventCount> {
        val clientId = AuthenticationUtils.clientID
        println("******************"+clientId)
        val clientSettings = clientSettingsRepository.findByClientID(clientId!!)
        println("***************************"+clientSettings)
        val timezone = TimeZone.getTimeZone(clientSettings!!.timezone)
        println("*****************"+timezone.id)
        return  reportServiceRepository.getEventsOfUserByDate(clientId!!, timezone, fromDate,toDate)
    }
}