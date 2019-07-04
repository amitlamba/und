package com.und.repository.mongo

import com.und.model.UpdateIdentity
import com.und.model.mongo.Event
import com.und.model.web.Identity
import java.util.*

interface EventUpdateCustomRepository {

    fun findEventsMatchingIdentity(identity: Identity):List<Event>

    fun updateEventsWithIdentityMatching(identity: Identity)

    fun updateEventsWithIdentityMatching(identity: UpdateIdentity)

    fun save(event: Event)

    fun findByName(eventName: String,clientId:Long): List<Event>

    fun findById(id:String,clientId: Long): Optional<Event>
}