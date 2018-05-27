package com.und.repository.mongo

import com.und.model.mongo.eventapi.Event
import com.und.web.model.eventapi.Identity

interface EventRepositoryUpdate {

    fun findEventsMatchingIdentity(identity: Identity):List<Event>

    fun updateEventsWithIdentityMatching(identity: Identity)
}