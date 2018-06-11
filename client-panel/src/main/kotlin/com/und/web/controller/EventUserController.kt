package com.und.web.controller

import com.und.security.utils.AuthenticationUtils
import com.und.service.EventUserService
import com.und.service.SegmentService
import com.und.web.controller.exception.EventNotFoundException
import com.und.web.controller.exception.EventUserListNotFoundException
import com.und.web.controller.exception.EventUserNotFoundException
import com.und.web.controller.exception.EventsListNotFoundException
import com.und.web.model.event.Event
import com.und.web.model.EventUser
import com.und.web.model.Response
import com.und.web.model.ResponseStatus
import com.und.web.model.Segment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*


@CrossOrigin
@Controller
@RequestMapping("/user")
class EventUserController {

    @Autowired
    private lateinit var eventUserService: EventUserService

    @Autowired
    private lateinit var segmentService: SegmentService


    @GetMapping(value = ["/google/{id}"])
    @ResponseBody
    fun findEventUserByGoogleId(@PathVariable id: String): ResponseEntity<EventUser> {

        val eventUser = eventUserService.findEventUserByGoogleId(id)
        return if (eventUser == null) {
            throw EventUserListNotFoundException("user with google id $id not found")
        } else {
            ResponseEntity(eventUser, HttpStatus.OK)
        }


    }

    @GetMapping(value = ["/id/{id}"])
    @ResponseBody
    fun findEventUserById(@PathVariable id: String): ResponseEntity<EventUser> {

        val eventUser = eventUserService.findEventUserById(id)
        return if (eventUser == null) {
            throw EventUserNotFoundException("user with id $id not found")
        } else {
            ResponseEntity(eventUser, HttpStatus.OK)
        }


    }

    @GetMapping(value = ["/mobile/{id}"])
    @ResponseBody
    fun findEventUserByMobile(@PathVariable id: String): ResponseEntity<EventUser> {

        val eventUser = eventUserService.findEventUserByMobile(id)
        return if (eventUser == null) {
            throw EventUserNotFoundException("user with mobile $id not found")
        } else {
            ResponseEntity(eventUser, HttpStatus.OK)
        }


    }

    @GetMapping(value = ["/fb/{id}"])
    @ResponseBody
    fun findEventUserByFB(@PathVariable id: String): ResponseEntity<EventUser> {

        val eventUser = eventUserService.findEventUserByFB(id)
        return if (eventUser == null) {
            throw EventUserNotFoundException("user with facebook id $id not found")
        } else {
            ResponseEntity(eventUser, HttpStatus.OK)
        }


    }

    @GetMapping(value = ["/sys/{id}"])
    @ResponseBody
    fun findEventUserBySysId(@PathVariable id: String): ResponseEntity<EventUser> {

        val eventUser = eventUserService.findEventUserBySysId(id)
        return if (eventUser == null) {
            throw EventUserNotFoundException("user with sys id $id not found")
        } else {
            ResponseEntity(eventUser, HttpStatus.OK)
        }


    }

    @GetMapping(value = ["/email/{id}"])
    @ResponseBody
    fun findEventUserByEmail(@PathVariable id: String): ResponseEntity<EventUser> {

        val eventUser = eventUserService.findEventUserByEmail(id)
        return if (eventUser == null) {
            throw EventUserNotFoundException("user with email id $id not found")
        } else {
            ResponseEntity(eventUser, HttpStatus.OK)
        }


    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = ["/user-list/segment"])
    fun findEventUsersBySegment(@RequestBody segment: Segment): ResponseEntity<List<EventUser>> {
        val clientId = getClientId()
        val eventUserList = segmentService.segmentUsers(segment, clientId)
        return if (eventUserList.isEmpty()) {
            throw EventUserListNotFoundException("users with segment ${segment.name} not found")
        } else {
            ResponseEntity(eventUserList, HttpStatus.OK)
        }
    }

    @GetMapping(value = ["/testuser/{id}"])
    @ResponseBody
    fun testUserProfile(@PathVariable id: String): ResponseEntity<Response> {
        val isTestUser = eventUserService.testUserProfile(id)
        return if (isTestUser == null) {
            throw EventUserNotFoundException("user with id $id not found")
        } else {
            ResponseEntity.ok().body(Response(
                    status = ResponseStatus.SUCCESS,
                    message = "User profile updated successfully"
            ))
        }
    }


    @GetMapping(value = ["/event-details/{id}"])
    @ResponseBody
    fun getEventDetailsById(@PathVariable id: String): ResponseEntity<Event> {

        val eventDetails = eventUserService.findEventDetailsById(id)
        return if (eventDetails == null) {
            throw EventNotFoundException("Event with id $id not found")
        } else {
            ResponseEntity(eventDetails, HttpStatus.OK)
        }
    }

    @GetMapping(value = ["/event-list/{id}"])
    @ResponseBody
    fun getEventsListByUserId(@PathVariable id: String): ResponseEntity<List<Event>> {

        val eventList = eventUserService.findEventsListById(id)
        return if (eventList==null) {
            throw EventsListNotFoundException("Events with id $id not found")
        } else {
            ResponseEntity(eventList, HttpStatus.OK)
        }
    }


    private fun getClientId(): Long {
        val clientId = AuthenticationUtils.clientID
        return clientId ?: throw org.springframework.security.access.AccessDeniedException("User is not logged in")

    }

}