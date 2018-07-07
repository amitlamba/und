package com.und.repository.mongo

import com.und.web.model.EventCount
import com.und.web.model.event.Event
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface ReportsServiceRepository{

    fun getEventsOfUserByDate(clientId: Long, timeZone: TimeZone, fromDate: String, toDate: String): List<EventCount>
}