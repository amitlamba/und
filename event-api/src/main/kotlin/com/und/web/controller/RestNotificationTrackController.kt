package com.und.web.controller

import com.und.service.FcmService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
public class RestNotificationTrackController{

    @Autowired
    private lateinit var fcmService: FcmService

    @GetMapping(value="/android/tracking/{mongoId}/{clientId}")

    fun trackAndroidFcmMessage(@PathVariable(required = true)mongoId:String, @PathVariable(required = true)clientId:Long){
        fcmService.updateStatus(mongoId,clientId,"android")
    }

    @RequestMapping(value ="/webpush/tracking/{mongoId}/{clientId}",method =[RequestMethod.GET],consumes = ["application/json"])
    fun trackWebpushFcmMessage(@PathVariable(required = true)mongoId:String, @PathVariable(required = true)clientId:Long){
        fcmService.updateStatus(mongoId,clientId,"web");
    }

    @GetMapping(value ="/ios/tracking/{mongoId}/{clientId}")
    fun trackIosFcmMessage(@PathVariable(required = true)mongoId:String, @PathVariable(required = true)clientId:Long){
        fcmService.updateStatus(mongoId,clientId,"ios")
    }

}