package com.und.service

import com.und.repository.mongo.EventRepository
import com.und.repository.mongo.EventUserRepository
import com.und.security.utils.AuthenticationUtils
import com.und.web.model.event.Event
import com.und.model.mongo.eventapi.EventUser as EventUserMongo
import com.und.model.mongo.eventapi.Event as EventMongo
import com.und.web.model.EventUser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class EventUserService {

    @Autowired
    private lateinit var eventUserRepository: EventUserRepository

    @Autowired
    private lateinit var eventRepository: EventRepository

    fun findEventUserById(id: String): EventUser? {
        val clientId = getClientId()
        val user = eventUserRepository.findUserById(id, clientId)
        return if (user.isPresent) {
            buildEventUser(user.get())
        } else null
    }

    fun findEventUserByEmail(id: String): EventUser? {
        val clientId = getClientId()
        val user = eventUserRepository.findUserByEmail(id, clientId)
        return if (user.isPresent) {
            buildEventUser(user.get())
        } else null
    }

    fun findEventUserBySysId(id: String): EventUser? {
        val clientId = getClientId()
        val user = eventUserRepository.findUserBySysId(id, clientId)
        return if (user.isPresent) {
            buildEventUser(user.get())
        } else null
    }

    fun findEventUserByFB(id: String): EventUser? {
        val clientId = getClientId()
        val user = eventUserRepository.findUserByFbId(id, clientId)
        return if (user.isPresent) {
            buildEventUser(user.get())
        } else null
    }

    fun findEventUserByMobile(id: String): EventUser? {
        val clientId = getClientId()
        val user = eventUserRepository.findUserByMobile(id, clientId)
        return if (user.isPresent) {
            buildEventUser(user.get())
        } else null
    }

    fun findEventUserByGoogleId(id: String): EventUser? {
        val clientId = getClientId()
        val user = eventUserRepository.findUserByGoogleId(id, clientId)
        return if (user.isPresent) {
            buildEventUser(user.get())
        } else null
    }

    fun testUserProfile(id: String): Unit? {
        val clientId = getClientId()
        val user = eventUserRepository.findUserById(id, clientId)
        return if (user.isPresent) {
            eventUserRepository.testUserProfile(id, clientId, user.get())
        } else null
    }

    fun findEventDetailsById(id: String): Event? {
        val clientId = getClientId()
        val event = eventRepository.findEventById(id, clientId)
        return if (event.isPresent) {
            buildEvent(event.get())
        } else null
    }

    fun findEventsListById(id: String):List<Event>?{
        val clientId = getClientId()
        val eventsListMongo=eventRepository.findEventsListById(id,clientId)
        return if(eventsListMongo.isNotEmpty()) {
            buildEventList(eventsListMongo)
        }else null
    }

    private fun getClientId(): Long {
        val clientId = AuthenticationUtils.clientID
        return clientId ?: throw org.springframework.security.access.AccessDeniedException("User is not logged in")

    }

    internal fun buildEventUser(eventUserMongo: EventUserMongo): EventUser {

        val eventUser = EventUser()
        eventUser.firstName = eventUserMongo.standardInfo.firstname
        eventUser.lastName = eventUserMongo.standardInfo.lastname
        eventUser.gender = eventUserMongo.standardInfo.gender
        eventUser.country = eventUserMongo.standardInfo.country
        eventUser.city = eventUserMongo.standardInfo.city
        eventUser.address = eventUserMongo.standardInfo.address
        eventUser.clientId = eventUserMongo.clientId
        eventUser.additionalInfo = eventUserMongo.additionalInfo
        eventUser.clientUserId = eventUserMongo.identity.clientUserId
        eventUser.dob = eventUserMongo.standardInfo.dob
        eventUser.creationDate = eventUserMongo.creationTime
        eventUser.email = eventUserMongo.identity.email
        eventUser.fbId = eventUserMongo.identity.fbId
        eventUser.googleId = eventUserMongo.identity.googleId
        eventUser.mobile = eventUserMongo.identity.mobile
        eventUser.undId = eventUserMongo.identity.undId
        eventUser.countryCode = eventUserMongo.standardInfo.countryCode
        eventUser.communication = eventUserMongo.communication
        return eventUser
    }

    private fun buildEvent(eventMongo: EventMongo): Event {
        val event = Event()
        event.name = eventMongo.name
        event.attributes = eventMongo.attributes
        event.city = eventMongo.geogrophy?.city
        event.state = eventMongo.geogrophy?.state
        event.country = eventMongo.geogrophy?.country
        event.lineItem = eventMongo.lineItem
        event.latitude = eventMongo.geoDetails.geolocation?.coordinate?.latitude
        event.longitude = eventMongo.geoDetails.geolocation?.coordinate?.longitude
        event.ipAddress = eventMongo.geoDetails.ip
        event.agentString = eventMongo.agentString
        event.clientId = eventMongo.clientId
        event.creationTime = eventMongo.creationTime
        event.userIdentified = eventMongo.userIdentified
        return event
    }

    private fun buildEventList(eventListMongo:List<EventMongo>):List<Event>{
        var eventList:List<Event> = emptyList()
        for(event in eventListMongo){
            val listElement=buildEvent(event)
            eventList+=listElement
        }
    return eventList
    }
}
