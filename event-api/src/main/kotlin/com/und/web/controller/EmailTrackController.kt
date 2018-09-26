package com.und.web.controller

import com.und.service.EmailService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@CrossOrigin
@Controller
@RequestMapping("/email")
class EmailTrackController {

    @Autowired
    private lateinit var emailService: EmailService

    @GetMapping(value = ["/image/{id}"], headers = arrayOf("Accept=image/jpeg, image/jpg, image/png, image/gif"))
    @ResponseBody
    fun getImage(@PathVariable id: String): ByteArray {
        return emailService.getImage(id)
    }

}