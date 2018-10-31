package com.und.web.controller

import com.und.model.jpa.WebPushTemplate
import com.und.security.utils.AuthenticationUtils
import com.und.service.WebPushService
import org.springframework.beans.factory.annotation.Autowired
import com.und.web.model.WebPushTemplate as WebTemplate
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/webpush")
class WebPushController {

    @Autowired
    private lateinit var webPushService: WebPushService
    @PostMapping("/save")
    fun saveTemplate(@Valid @RequestBody template:WebTemplate):WebPushTemplate?{
        //check clientid
        var clientId=AuthenticationUtils.clientID
        if(clientId!=null) {
            var isExists=webPushService.isTemplateExists(clientId, template.name)
            if(isExists){
                //throw error already exists
            }
        }
        else {
            return null //throw exception clientid is null
        }
        return webPushService.saveTemplate(template)
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