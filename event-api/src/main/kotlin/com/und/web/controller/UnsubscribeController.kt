package com.und.web.controller

import com.und.security.model.Data
import com.und.security.model.Response
import com.und.security.model.ResponseStatus
import com.und.service.eventapi.UnsubscribeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
class UnsubscribeController {

    @Autowired
    private lateinit var unsubscribeService: UnsubscribeService

    @GetMapping(value = ["/email/unsubscribe"], produces = ["application/json"])
    fun unsubscribeUserFromEmail(@RequestParam("c") clientId: Int, @RequestParam("e") mongoEmailId: String)
    : ResponseEntity<Response<Boolean>>{

        val unsubscribed = unsubscribeService.unsubscribeUserFromEmail(clientId, mongoEmailId)

        if(!unsubscribed) {
            return ResponseEntity.badRequest().build()
        }

        return ResponseEntity.ok(Response(
                status = ResponseStatus.SUCCESS,
                data = Data<Boolean>(true, "Successfully Unsubscribed")
        ))
    }
}