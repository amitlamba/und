package com.und.web.controller

import com.und.service.EventUserService
import com.und.web.controller.exception.EventUserNotFoundException
import com.und.web.model.EventUser
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


    @GetMapping(value = ["/google/{id}"])
    @ResponseBody
    fun findEventUserByGoogleId(@PathVariable id: String): ResponseEntity<EventUser> {

        val eventUser = eventUserService.findEventUserByGoogleId(id)
        return if (eventUser == null) {
            throw EventUserNotFoundException()
        } else {
            ResponseEntity(eventUser, HttpStatus.OK)
        }


    }
    @GetMapping(value = ["/id/{id}"])
    @ResponseBody
    fun findEventUserById(@PathVariable id: String): ResponseEntity<EventUser> {

        val eventUser = eventUserService.findEventUserById(id)
        return if (eventUser == null) {
            throw EventUserNotFoundException()
        } else {
            ResponseEntity(eventUser, HttpStatus.OK)
        }


    }

    @GetMapping(value = ["/mobile/{id}"])
    @ResponseBody
    fun findEventUserByMobile(@PathVariable id: String): ResponseEntity<EventUser> {

        val eventUser = eventUserService.findEventUserByMobile(id)
        return if (eventUser == null) {
            throw EventUserNotFoundException()
        } else {
            ResponseEntity(eventUser, HttpStatus.OK)
        }


    }

    @GetMapping(value = ["/fb/{id}"])
    @ResponseBody
    fun findEventUserByFB(@PathVariable id: String): ResponseEntity<EventUser> {

        val eventUser = eventUserService.findEventUserByFB(id)
        return if (eventUser == null) {
            throw EventUserNotFoundException()
        } else {
            ResponseEntity(eventUser, HttpStatus.OK)
        }


    }

    @GetMapping(value = ["/sys/{id}"])
    @ResponseBody
    fun findEventUserBySysId(@PathVariable id: String): ResponseEntity<EventUser> {

        val eventUser = eventUserService.findEventUserBySysId(id)
        return if (eventUser == null) {
            throw EventUserNotFoundException()
        } else {
            ResponseEntity(eventUser, HttpStatus.OK)
        }


    }

    @GetMapping(value = ["/email/{id}"])
    @ResponseBody
    fun findEventUserByEmail(@PathVariable id: String): ResponseEntity<EventUser> {

        val eventUser = eventUserService.findEventUserByEmail(id)
        return if (eventUser == null) {
            throw EventUserNotFoundException()
        } else {
            ResponseEntity(eventUser, HttpStatus.OK)
        }


    }
}