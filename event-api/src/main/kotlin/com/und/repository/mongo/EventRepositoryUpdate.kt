package com.und.repository.mongo

import com.und.model.UpdateIdentity
import com.und.model.mongo.eventapi.Event
import com.und.service.eventapi.EventUserService
import com.und.web.model.eventapi.Identity
import java.util.*

interface EventRepositoryUpdate {

    fun findEventsMatchingIdentity(identity: Identity):List<Event>

    fun updateEventsWithIdentityMatching(identity: Identity)

    fun updateEventsWithIdentityMatching(identity: UpdateIdentity)

    fun save(event:Event)

    fun findByName(eventName: String,clientId:Long): List<Event>

    fun findById(id:String,clientId: Long): Optional<Event>
}