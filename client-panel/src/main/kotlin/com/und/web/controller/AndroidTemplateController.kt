package com.und.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.model.jpa.Action
import com.und.model.jpa.AndroidTemplate
import com.und.repository.jpa.AndroidRepository
import com.und.security.utils.AuthenticationUtils
import com.und.service.AndroidService
import com.und.web.controller.exception.CustomException
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
    private lateinit var androidService: AndroidService
    @Autowired
    private lateinit var androidRepository: AndroidRepository

    @PreAuthorize(value="hasRole('ROLE_ADMIN')")
    @PostMapping("/save")
    fun saveTemplate(@Valid @RequestBody template:WebAndroidTemplate):ResponseEntity<WebAndroidTemplate>{
        var clientId= AuthenticationUtils.clientID?: throw AccessDeniedException("")
//            var result = androidRepository.findByClientIdAndName(clientId, template.name)
        var result=androidRepository.existsByClientIdAndName(clientId,template.name)
//            if(result.isNotEmpty()) {
//                throw CustomException("Template with name ${template.name} already exists")
//            }
        return if(result) throw CustomException("Template with name ${template.name} already exists")
        //if exception thrown show properly
        else  ResponseEntity(androidService.save(template),HttpStatus.CREATED)
    }
    @PreAuthorize(value="hasRole('ROLE_ADMIN')")
    @GetMapping("/templates")
    fun getTemplates():List<WebAndroidTemplate>{
        var clientId=AuthenticationUtils.clientID?:throw AccessDeniedException("")
        return androidService.getAllAndroidTemplate(clientId)
    }
    @PreAuthorize(value="hasRole('ROLE_ADMIN')")
    @GetMapping("/template/{id}")
    fun getTemplateById(@PathVariable id:Long):WebAndroidTemplate?{
        var clientId=AuthenticationUtils.clientID?: throw AccessDeniedException("")
        return androidService.getAndroidTemplateById(clientId,id)
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/actions")
    fun getAllAction():List<com.und.web.model.Action>{
        var clientId=AuthenticationUtils.clientID?: throw AccessDeniedException("")
        return androidService.getAllAndroidAction(clientId)
    }
}