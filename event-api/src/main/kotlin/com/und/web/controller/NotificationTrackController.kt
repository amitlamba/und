package com.und.web.controller

import com.und.service.EmailService
import com.und.service.FcmService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

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

    @GetMapping(value ="/android/tracking/{mongoId}/{clientId}")
    fun trackAndroidFcmMessage(@PathVariable(required = true)mongoId:String,@PathVariable(required = true)clientId:Long){
        fcmService.updateStatus(mongoId,clientId,"android")
    }

    @GetMapping(value ="/webpush/tracking/{mongoId}/{clientId}")
    fun trackWebpushFcmMessage(@PathVariable(required = true)mongoId:String,@PathVariable(required = true)clientId:Long){
        fcmService.updateStatus(mongoId,clientId,"web");
    }

    @GetMapping(value ="/ios/tracking/{mongoId}/{clientId}")
    fun trackIosFcmMessage(@PathVariable(required = true)mongoId:String,@PathVariable(required = true)clientId:Long){
        fcmService.updateStatus(mongoId,clientId,"ios")
    }
}