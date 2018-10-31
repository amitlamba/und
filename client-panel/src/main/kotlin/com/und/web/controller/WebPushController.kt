package com.und.web.controller

import com.und.model.jpa.WebPushTemplate
import com.und.security.utils.AuthenticationUtils
import com.und.service.WebPushService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import com.und.web.model.WebPushTemplate as WebTemplate
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/webpush")
class WebPushController {

    @Autowired
    private lateinit var webPushService: WebPushService
    @PostMapping("/save")
    fun saveTemplate(@Valid @RequestBody template:WebTemplate):ResponseEntity<WebPushTemplate?>{
        //check clientid
        var clientId=AuthenticationUtils.clientID
        if(clientId!=null) {
            var isExists=webPushService.isTemplateExists(clientId, template.name)
            if(isExists){
                ResponseEntity(template,HttpStatus.EXPECTATION_FAILED)
            }
        }
        else {
            throw AccessDeniedException("")
        }
        return ResponseEntity(webPushService.saveTemplate(template), HttpStatus.CREATED)
    }
    @GetMapping("/template/{id}")
    fun getTemplate(@PathVariable id:Long):WebPushTemplate?{
        return webPushService.getTemplate(id)
    }
    @GetMapping("/templates")
    fun getAllTemplate():List<WebPushTemplate>{
        return webPushService.getAllTemplate()
    }
}