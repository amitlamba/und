package com.und.web.controller

import com.und.common.utils.decrypt
import com.und.common.utils.encrypt
import com.und.model.EmailRead
import com.und.service.EmailService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*
import java.io.IOException
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.net.URLDecoder
import java.net.URLEncoder

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