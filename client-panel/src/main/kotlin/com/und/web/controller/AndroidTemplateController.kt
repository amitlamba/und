package com.und.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.model.jpa.Action
import com.und.model.jpa.AndroidTemplate
import com.und.repository.jpa.AndroidRepository
import com.und.security.utils.AuthenticationUtils
import com.und.service.AndroidService
import com.und.web.model.AndroidTemplate as WebAndroidTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/android")
class AndroidTemplateController {

    @Autowired
    private lateinit var objectmapper:ObjectMapper
    @Autowired
    private lateinit var androidService: AndroidService
    @Autowired
    private lateinit var androidRepository: AndroidRepository

    @PreAuthorize(value="hasRole('ROLE_ADMIN')")
    @PostMapping("/save")
    fun saveTemplate(@Valid @RequestBody template:WebAndroidTemplate):ResponseEntity<AndroidTemplate>{
        var clientId= AuthenticationUtils.clientID
        if(clientId!=null) {
            var result = androidRepository.findByClientIdAndName(clientId, template.name)
            if(result.isNotEmpty()){
                println("exists already")
                //throw exception already exist
                ResponseEntity(template,HttpStatus.EXPECTATION_FAILED)
            }

        }else{
            throw AccessDeniedException("")
        }
        return  ResponseEntity(androidService.save(template),HttpStatus.CREATED)
    }
    @PreAuthorize(value="hasRole('ROLE_ADMIN')")
    @GetMapping("/templates")
    fun getTemplates():List<AndroidTemplate>{
        var clientId=AuthenticationUtils.clientID?:throw AccessDeniedException("")
        return androidService.getAllAndroidTemplate(clientId)
    }
    @PreAuthorize(value="hasRole('ROLE_ADMIN')")
    @GetMapping("/template/{id}")
    fun getTemplateById(@PathVariable id:Long):AndroidTemplate?{
        var clientId=AuthenticationUtils.clientID?: throw AccessDeniedException("")
        return androidService.getAndroidTemplateById(clientId,id)
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/actions")
    fun getAllAction():List<Action>{
        var clientId=AuthenticationUtils.clientID?: throw AccessDeniedException("")
        return androidService.getAllAndroidAction(clientId)
    }
}