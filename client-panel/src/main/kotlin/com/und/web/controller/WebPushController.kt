package com.und.web.controller

import com.und.model.jpa.WebPushTemplate
import com.und.security.utils.AuthenticationUtils
import com.und.service.WebPushService
import com.und.web.controller.exception.CustomException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
import com.und.web.model.WebPushTemplate as WebTemplate
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/webpush")
class WebPushController {

    @Autowired
    private lateinit var webPushService: WebPushService
    @PreAuthorize(value="hasRole('ROLE_ADMIN')")
    @PostMapping("/save")
    fun saveTemplate(@Valid @RequestBody template:WebTemplate):ResponseEntity<WebTemplate>{
        var clientId=AuthenticationUtils.clientID?: throw AccessDeniedException("")
            var isExists=webPushService.isTemplateExists(clientId, template.name)
            if(isExists){
                throw CustomException("Template with name ${template.name} already exists")
            }
        return ResponseEntity(webPushService.saveTemplate(template), HttpStatus.CREATED)
    }
    @PreAuthorize(value="hasRole('ROLE_ADMIN')")
    @GetMapping("/template/{id}")
    fun getTemplate(@PathVariable id:Long):WebTemplate{
        return webPushService.getTemplate(id)
    }
    @PreAuthorize(value="hasRole('ROLE_ADMIN')")
    @GetMapping("/templates")
    fun getAllTemplate():List<WebTemplate>{
        return webPushService.getAllTemplate()
    }
}