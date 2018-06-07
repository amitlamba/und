package com.und.web.controller

import com.und.security.utils.AuthenticationUtils
import com.und.service.EventUserService
import com.und.service.SegmentService
import com.und.web.controller.exception.EventUserListNotFoundException
import com.und.web.controller.exception.EventUserNotFoundException
import com.und.web.model.EventUser
import com.und.model.mongo.eventapi.EventUser as MongoEventUser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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

    @GetMapping(value = ["/segment/{id}"])
    @ResponseBody
    fun findEventUsersBySegment(@PathVariable id: Long): ResponseEntity<List<MongoEventUser>> {
        val clientId = getClientId()
        val eventUserList = segmentService.segmentUsers(id,clientId)
        return if (eventUserList.isEmpty()) {
            throw EventUserListNotFoundException("users with segment id $id not found")
        } else {
            ResponseEntity(eventUserList, HttpStatus.OK)
        }
    }

    private fun getClientId(): Long {
        val clientId = AuthenticationUtils.clientID
        return clientId?:throw org.springframework.security.access.AccessDeniedException("User is not logged in")

    }

}