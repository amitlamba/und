package com.und.repository.mongo

import com.und.model.mongo.eventapi.EventUser
import java.util.*

interface EventUserCustomRepository {
    fun findById(id: String,clientId: Long): Optional<EventUser>
    fun save(eventUser: EventUser): EventUser
//    fun deleteById(id: String,clientId: Long)
//    fun findByIdOrIdentityUid(id: String, uid: String,clientId:Long): Optional<EventUser>
//    fun findByIdAndIdentityUid(id: String, uid: String,clientId:Long): Optional<EventUser>
    fun findByIdentityUid(uid: String,clientId:Long): Optional<EventUser>
}