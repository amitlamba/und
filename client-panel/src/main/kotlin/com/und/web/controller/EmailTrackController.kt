package com.und.web.controller

import com.und.common.utils.decrypt
import com.und.common.utils.encrypt
import com.und.model.EmailRead
import com.und.service.EmailService
import org.springframework.beans.factory.annotation.Autowired
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
        try {
            println(id)
            println(URLDecoder.decode(id,"UTF-8"))
            println(decrypt(URLDecoder.decode(id,"UTF-8")))
            val imageLink: String? = decrypt(URLDecoder.decode(id,"UTF-8"))
            emailService.trackEmailRead(EmailRead(1, imageLink!!)) //TODO: clientID hard coded
            var bufferedImage = BufferedImage(1, 1,
                    BufferedImage.TYPE_INT_ARGB)
            val byteArrayOutputStream = ByteArrayOutputStream()
            ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream)
            return byteArrayOutputStream.toByteArray()

        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @GetMapping(value = ["/create-image/{id}"])
    @ResponseBody
    fun getImagePath(@PathVariable id: String): String {
        return "http://localhost:8080/email/image/${URLEncoder.encode(URLEncoder.encode(encrypt(id),"UTF-8"),"UTF-8")}.jpg"
    }
}