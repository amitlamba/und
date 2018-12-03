package com.und.web.controller

import com.und.service.EmailService
import com.und.service.FcmService
import org.apache.kafka.common.protocol.types.ArrayOf
import org.springframework.beans.factory.annotation.Autowired
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


    @GetMapping(value = ["/email/image/{id}"], headers = arrayOf("Accept=image/jpeg, image/jpg, image/png, image/gif"))
    @ResponseBody
    fun getImage(@PathVariable id: String): ByteArray {
        return emailService.getImage(id)
    }
}