package com.und.web.controller

import com.und.model.IncludeUsers
import com.und.security.utils.AuthenticationUtils
import com.und.service.EventUserService
import com.und.service.SegmentService
import com.und.web.controller.exception.EventUserListBySegmentNotFoundException
import com.und.web.model.EventUserMinimal
import com.und.web.model.EventUser
import com.und.web.model.Response
import com.und.web.model.Segment
import com.und.web.model.event.Event
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest


@CrossOrigin
@RestController
@RequestMapping("/user")
class EventUserController {

    @Autowired
    private lateinit var eventUserService: EventUserService

    @Autowired
    private lateinit var segmentService: SegmentService


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/google/{id}"])
    fun findEventUserByGoogleId(@PathVariable id: String): ResponseEntity<EventUser> {
        val eventUser = eventUserService.findEventUserByGoogleId(id)
        return ResponseEntity(eventUser, HttpStatus.OK)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/id/{id}"])
    fun findEventUserById(@PathVariable id: String): ResponseEntity<EventUser> {
        val eventUser = eventUserService.findEventUserById(id)
        return ResponseEntity(eventUser, HttpStatus.OK)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/mobile/{id}"])
    fun findEventUserByMobile(@PathVariable id: String): ResponseEntity<EventUser> {
        val eventUser = eventUserService.findEventUserByMobile(id)
        return ResponseEntity(eventUser, HttpStatus.OK)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/fb/{id}"])
    fun findEventUserByFB(@PathVariable id: String): ResponseEntity<EventUser> {
        val eventUser = eventUserService.findEventUserByFB(id)
        return ResponseEntity(eventUser, HttpStatus.OK)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/sys/{id}"])
    @ResponseBody
    fun findEventUserBySysId(@PathVariable id: String): ResponseEntity<EventUser> {
        val eventUser = eventUserService.findEventUserBySysId(id)
        return ResponseEntity(eventUser, HttpStatus.OK)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/email/{id}"])
    fun findEventUserByEmail(@PathVariable id: String): ResponseEntity<EventUser> {
        val eventUser = eventUserService.findEventUserByEmail(id)
        return ResponseEntity(eventUser, HttpStatus.OK)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = ["/user-list/segment"])
    fun findEventUsersBySegment(@RequestBody segment: Segment,request:HttpServletRequest): ResponseEntity<List<EventUserMinimal>> {
        val clientId = AuthenticationUtils.retrieveClientId()
        val appuserID = AuthenticationUtils.principal.id!!
        val includeUsers=request.getParameter("include")?:"ALL"
        val eventUserList = segmentService.segmentUsers(segment, clientId, appuserID,IncludeUsers.valueOf(includeUsers))
        return if (eventUserList.isEmpty()) {
            throw EventUserListBySegmentNotFoundException("Event user list not found")
        } else {
            ResponseEntity(eventUserList, HttpStatus.OK)
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(value = ["/setTestProfile/{id}"])
    fun setTestProfile(@PathVariable id: String): ResponseEntity<Response> {
        eventUserService.setTestProfile(id)
        return ResponseEntity(HttpStatus.OK)

    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PatchMapping(value = ["/unsetTestProfile/{id}"])
    fun unsetTestProfile(@PathVariable id: String): ResponseEntity<Response> {
        eventUserService.unsetTestProfile(id)
        return ResponseEntity(HttpStatus.OK)

    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/event-details/{id}"])
    fun getEventDetailsById(@PathVariable id: String): ResponseEntity<Event> {
        val eventDetails = eventUserService.findEventDetailsById(id)
        return ResponseEntity(eventDetails, HttpStatus.OK)
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/event-list/{id}"])
    fun getEventsListByUserId(@PathVariable id: String): ResponseEntity<List<Event>> {
        val eventList = eventUserService.findEventsListById(id)
        return ResponseEntity(eventList, HttpStatus.OK)
    }




}