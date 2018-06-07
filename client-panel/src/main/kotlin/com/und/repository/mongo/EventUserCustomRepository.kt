package com.und.repository.mongo

import com.und.model.mongo.eventapi.EventUser
import java.util.*

interface EventUserCustomRepository {

    fun findUserById(id: String, clientId: Long): Optional<EventUser>
    fun findUserByGoogleId(id: String, clientId: Long): Optional<EventUser>
    fun findUserByFbId(id: String, clientId: Long): Optional<EventUser>
    fun findUserBySysId(id: String, clientId: Long): Optional<EventUser>
    fun findUserByEmail(id: String, clientId: Long): Optional<EventUser>
    fun findUserByMobile(id: String, clientId: Long): Optional<EventUser>

}