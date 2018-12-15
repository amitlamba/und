package com.und.web.controller.eventapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.web.model.eventapi.Event
import com.und.web.model.eventapi.EventUser
import com.und.web.model.eventapi.Identity
import com.und.service.eventapi.EventService
import com.und.service.eventapi.EventUserService
import com.und.eventapi.utils.ipAddr
import com.und.security.model.Data
import com.und.security.model.Response
import com.und.security.model.ResponseStatus
import com.und.security.utils.TenantProvider
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@CrossOrigin
@RestController
class EventRestController {

    @Autowired
    private lateinit  var eventService: EventService

    @Autowired
    private lateinit var eventUserService: EventUserService

    @Autowired
    private lateinit var tenantProvider: TenantProvider



    @PreAuthorize("hasRole('ROLE_EVENT')")
    @PostMapping(value = ["/event/initialize"], produces = ["application/json"], consumes =["application/json"])
    fun initialize(@Valid @RequestBody identity: Identity?): ResponseEntity<Response<Identity>> {

        return ResponseEntity.ok(Response(
                status = ResponseStatus.SUCCESS,
                data = Data(eventUserService.initialiseIdentity(identity))
        ))
    }

    @PreAuthorize("hasRole('ROLE_EVENT')")
    @PostMapping(value = ["/push/event"], produces = ["application/json"], consumes = ["application/json"])
    fun saveEvent(@Valid @RequestBody event: Event, request: HttpServletRequest): ResponseEntity<Response<String>> {
        var headers=request.headerNames
       println( request.remoteHost)
        println(request.remoteUser)
        println(request.serverName)
        println(request.serverPort)
        if(headers.hasMoreElements()){
            print(headers.nextElement())
        }
        val toEvent = eventService.buildEvent(event, request)
        eventService.toKafka(toEvent)
        return ResponseEntity.ok(Response(status = ResponseStatus.SUCCESS))
    }

    @PreAuthorize("hasRole('ROLE_EVENT')")
    @PostMapping(value = ["/push/profile"], produces = ["application/json"], consumes = ["application/json"])
    fun profile(@Valid @RequestBody eventUser: EventUser): ResponseEntity<Response<Identity>> {

        //this method can't be called before identity has been initialized
        val identityInit = eventUserService.initialiseIdentity(eventUser.identity)

        //if only uid or userid present find if it exists
        //if exists do below
        val userId = eventUser.identity.userId
        val uid = eventUser.uid
        val mongoUser = when {
            userId != null && uid == null -> {
                eventUserService.getEventUserByEventUserId(userId)
            }
            userId == null && uid != null -> {
                eventUserService.getEventUserByUid(uid)
            }
            userId != null && uid != null -> {

                eventUserService.getEventUserByEventUserIdAndUid(userId, uid)
            }
            else -> null

        }

        if (mongoUser != null) {
            identityInit.userId = mongoUser.id
            eventUser.uid = mongoUser.identity.uid
        } else {
            identityInit.userId = identityInit.userId ?: ObjectId.get().toString()
            eventUser.undId=identityInit.userId
        }



        identityInit.clientId = tenantProvider.tenant.toInt()
        eventUser.identity = identityInit
        eventUserService.toKafka(eventUser)
        //don't send event back rather send instance id, and status, also send a new instance id if user id changes
        return ResponseEntity.ok(Response(
                status = ResponseStatus.SUCCESS,
                data = Data(identityInit)
        ))
    }

    @PreAuthorize("hasRole('ROLE_EVENT')")
    @GetMapping("/check")
    fun checkConnection(): ResponseEntity<Response<String>> {
        return ResponseEntity.ok(Response(status = ResponseStatus.SUCCESS))
    }

/*
    @RequestMapping(value = "/event/{name}", produces =["application/json"], method = [RequestMethod.GET])
    fun getEvent(@PathVariable("name") name: String): ResponseEntity<Response<List<Event>>> {
        val events = eventService.findByName(name)
        return ResponseEntity.ok(Response(
                status = ResponseStatus.SUCCESS,
                data = Data(events)
        ))
    }*/


}
