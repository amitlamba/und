package com.und.repository.mongo

import com.und.model.mongo.eventapi.EventUser
import java.util.*

interface EventUserCustomRepository {
    fun findUserById(id:String, clientId:Long): Optional<EventUser>

    fun findUserByGoogleId(id: String, clientId: Long): Optional<EventUser>
}