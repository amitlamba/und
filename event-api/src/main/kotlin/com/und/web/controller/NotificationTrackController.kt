package com.und.web.controller

import com.und.security.utils.AuthenticationUtils
import com.und.service.AndroidService
import com.und.service.EmailService
import com.und.service.WebpushService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@CrossOrigin
@Controller
class NotificationTrackController {

    @Autowired
    private lateinit var emailService: EmailService
    @Autowired
    private lateinit var androidService:AndroidService
    @Autowired
    private lateinit var webpushService:WebpushService

    @GetMapping(value = ["/email/image/{id}"], headers = arrayOf("Accept=image/jpeg, image/jpg, image/png, image/gif"))
    @ResponseBody
    fun getImage(@PathVariable id: String): ByteArray {
        return emailService.getImage(id)
    }

    @PostMapping(value ="/android/tracking")
    fun trackAndroidFcmMessage(mongoId:String,clientId:Long){
        androidService.updateStatus(mongoId,clientId)
    }

    @PostMapping(value="/webpush/tracking")
    fun trackWebpushFcmMessage(mongoId: String,clientId: Long){
        webpushService.updateStatus(mongoId,clientId);
    }

}