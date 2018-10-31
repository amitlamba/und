package com.und.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.und.model.jpa.Action
import com.und.model.jpa.AndroidTemplate
import com.und.repository.jpa.AndroidRepository
import com.und.security.utils.AuthenticationUtils
import com.und.service.AndroidService
import com.und.web.model.AndroidTemplate as WebAndroidTemplate
import org.springframework.beans.factory.annotation.Autowired
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
    fun saveTemplate(@Valid @RequestBody template:WebAndroidTemplate):AndroidTemplate?{
        var clientId= AuthenticationUtils.clientID
        if(clientId!=null) {
            var result = androidRepository.findByClientIdAndName(clientId, template.name)
            if(result.isNotEmpty()){
                println("exists already")
                //throw exception already exist
            }

        }else{
            return null //or throw error
        }
        return  androidService.save(template)
    }
    @PreAuthorize(value="hasRole('ROLE_ADMIN')")
    @GetMapping("/templates")
    fun getTemplates():List<AndroidTemplate>{
        var clientId=AuthenticationUtils.clientID
        if(clientId!=null) return androidService.getAllAndroidTemplate(clientId) else return emptyList()
    }
    @PreAuthorize(value="hasRole('ROLE_ADMIN')")
    @GetMapping("/template/{id}")
    fun getTemplateById(@PathVariable id:Long):AndroidTemplate?{
        var clientId=AuthenticationUtils.clientID
        if(clientId!=null) return androidService.getAndroidTemplateById(clientId,id) else return null
    }
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/actions")
    fun getAllAction():List<Action>{
        var clientId=AuthenticationUtils.clientID
        if(clientId!=null) return androidService.getAllAndroidAction(clientId) else return emptyList<Action>()
    }
}