package com.und.web.controller

import com.und.model.mongo.eventapi.Event
import com.und.model.mongo.eventapi.EventUser
import com.und.security.utils.AuthenticationUtils
import com.und.service.ClientUsersService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
@RequestMapping("/client/users")
class ClientUsersController {

    @Autowired
    private lateinit var clientUsersService: ClientUsersService

    @GetMapping(value = ["/get-list"])
    fun getListOfUsers(): List<EventUser> {
        return clientUsersService.getEventUsers(AuthenticationUtils.clientID!!)
    }

    @GetMapping(value = ["/get-user-events"])
    fun getEventsOfAUser(userID: String, emailID: String, undUserID: String): List<Event> {
        //TODO Complete the method
        return listOf()
    }
}