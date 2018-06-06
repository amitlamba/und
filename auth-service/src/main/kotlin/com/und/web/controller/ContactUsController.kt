package com.und.web.controller

import com.und.service.ContactUsService
import com.und.web.model.ContactUs
import com.und.web.model.Response
import com.und.web.model.ResponseStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid


@CrossOrigin
@RestController
@RequestMapping("/contactUs")
class ContactUsController {

    @Autowired
    lateinit var contactUsService: ContactUsService

    @PostMapping("/save")
    fun saveContactUsDetails(@Valid @RequestBody contactInfo: ContactUs): ResponseEntity<Response> {
        contactUsService.save(contactInfo)
        return ResponseEntity.ok().body(Response(
                status = ResponseStatus.SUCCESS,
                message = "Submitted Successfully"
        ))

    }


}