package com.und.service

import com.und.common.utils.DateUtils
import com.und.repository.mongo.EventRepository
import com.und.repository.mongo.EventUserRepo
import com.und.repository.mongo.EventUserRepository
import com.und.security.utils.AuthenticationUtils
import com.und.web.controller.exception.EventNotFoundException
import com.und.web.controller.exception.EventUserListNotFoundException
import com.und.web.controller.exception.EventUserNotFoundException
import com.und.web.controller.exception.EventsListNotFoundException
import com.und.web.model.EventUser
import com.und.web.model.event.Event
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import com.und.model.mongo.eventapi.Event as EventMongo
import com.und.model.mongo.eventapi.EventUser as EventUserMongo


@Service
class EventUserService {

    @Autowired
    private lateinit var eventUserRepository: EventUserRepository

    @Autowired
    private lateinit var eventRepository: EventRepository

    @Autowired
    private lateinit var eventUserCustomRepository: EventUserRepo

    fun getTotalEventUserToday():Long{
        return eventUserCustomRepository.totalEventUserToday()
    }

    fun findEventUserById(id: String): EventUser {
        return findUser(id, eventUserRepository::findUserById)
    }

    fun findEventUserByEmail(id: String): EventUser {
        return findUser(id, eventUserRepository::findUserByEmail)
    }

    fun findEventUserBySysId(id: String): EventUser {
        return findUser(id, eventUserRepository::findUserBySysId)
    }

    fun findEventUserByFB(id: String): EventUser {
        return findUser(id, eventUserRepository::findUserByFbId)
    }

    fun findEventUserByMobile(id: String): EventUser {
        return findUser(id, eventUserRepository::findUserByMobile)
    }

    fun findEventUserByGoogleId(id: String): EventUser {
        return findUser(id, eventUserRepository::findUserByGoogleId)
    }

    private fun findUser(id: String, find: (id: String, clientId: Long) -> Optional<com.und.model.mongo.eventapi.EventUser>): EventUser {
        val clientId = getClientId()
        val user = find(id, clientId)
        return user.map { buildEventUser(it) }
                .orElseThrow{ EventUserListNotFoundException("user with provided id $id not found") }
    }

    fun unsetTestProfile(id: String) {
        setProfileAsTest(id, false)
    }

    fun setTestProfile(id: String) {
        setProfileAsTest(id, true)
    }

    fun setProfileAsTest(id: String, isTest: Boolean) {
        val clientId = getClientId()
        val user = eventUserRepository.findUserById(id, clientId)
        return if (user.isPresent) {
            eventUserRepository.testUserProfile(id, clientId, isTest)
        } else throw EventUserNotFoundException("user with id $id not found")
    }


    fun findEventDetailsById(id: String): Event {
        val clientId = getClientId()
        val event = eventRepository.findEventById(id, clientId)
        return event.map{buildEvent(it)}
                .orElseThrow{EventNotFoundException("Event with id $id not found")}

    }

    fun findEventsListById(id: String): List<Event> {
        val clientId = getClientId()
        val eventsListMongo = eventRepository.findEventsListById(id, clientId)

        return if (eventsListMongo.isNotEmpty()) {
            eventsListMongo.map { event->buildEvent(event) }
        } else throw EventsListNotFoundException("Events with id $id not found")
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
        eventUser.uid = eventUserMongo.identity.uid
        eventUser.dob = eventUserMongo.standardInfo.dob
        eventUser.creationDate = DateUtils().convertDateToDateTime(eventUserMongo.creationTime)
        eventUser.email = eventUserMongo.identity.email
        eventUser.fbId = eventUserMongo.identity.fbId
        eventUser.googleId = eventUserMongo.identity.googleId
        eventUser.mobile = eventUserMongo.identity.mobile
        eventUser.undId = eventUserMongo.id
        eventUser.countryCode = eventUserMongo.standardInfo.countryCode
        eventUser.communication = eventUserMongo.communication
        eventUser.testUser = eventUserMongo.testUser
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
        event.creationTime = DateUtils().convertDateToDateTime(eventMongo.creationTime)
        event.userIdentified = eventMongo.userIdentified
        return event
    }


}
