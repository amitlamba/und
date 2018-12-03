package com.und.web.controller

import com.und.service.FcmService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
public class RestNotificationTrackController{

    @Autowired
    private lateinit var fcmService: FcmService

    @PostMapping(value="/android/tracking")
    fun trackAndroidFcmMessage(@RequestBody track:Track){
        var mongoId=track.mongoId
        var clientId=track.clientId
        fcmService.updateStatus(mongoId,clientId,"android")
    }

    @PostMapping(value="/webpush/tracking")
    fun trackWebpushFcmMessage(@RequestBody track:Track){
        var mongoId=track.mongoId
        var clientId=track.clientId
        fcmService.updateStatus(mongoId,clientId,"web");
    }

    @PostMapping(value="/ios/tracking")
    fun trackIosFcmMessage(@RequestBody track:Track){
        var mongoId=track.mongoId
        var clientId=track.clientId
        fcmService.updateStatus(mongoId,clientId,"ios")
    }

}

class Track{
    lateinit var mongoId:String
    var clientId: Long=-1
}