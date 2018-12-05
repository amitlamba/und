package com.und.web.controller

import com.und.service.EmailService
import com.und.service.FcmService
import org.apache.kafka.common.protocol.types.ArrayOf
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.util.*

@CrossOrigin
@Controller
class NotificationTrackController {

    @Autowired
    private lateinit var emailService: EmailService
//    @Autowired
//    private lateinit var androidService:AndroidService

    @Autowired
    private lateinit var fcmService: FcmService

    @GetMapping(value = ["/email/image/{id}"], headers = arrayOf("Accept=image/jpeg, image/jpg, image/png, image/gif"))
    @ResponseBody
    fun getImage(@PathVariable id: String): ByteArray {
        return emailService.getImage(id)
    }

    @PostMapping(value="/android/tracking",headers = ["Accept=application/json"],consumes=["application/json"])
    @ResponseBody
    fun trackAndroidFcmMessage(@RequestBody track:Track){
        var mongoId=track.mongoId
        var clientId=track.clientId
        fcmService.updateStatus(mongoId,clientId,"android")
    }

    @PostMapping(value="/webpush/tracking",consumes = ["application/json"])
    @ResponseBody
    fun trackWebpushFcmMessage(@RequestBody track:Track){
        var mongoId=track.mongoId
        var clientId=track.clientId
        fcmService.updateStatus(mongoId,clientId,"web");
    }

    @PostMapping(value="/ios/tracking",consumes = ["application/json"])
    @ResponseBody
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