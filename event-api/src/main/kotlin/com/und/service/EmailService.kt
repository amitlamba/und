package com.und.service

import com.und.common.utils.encrypt
import com.und.config.EventStream
import com.und.model.EmailRead
import com.und.model.mongo.EmailStatus
import com.und.model.mongo.EmailStatusUpdate
import com.und.web.model.eventapi.Event
import com.und.repository.mongo.EmailSentRepository
import com.und.security.utils.TenantProvider
import com.und.service.eventapi.EventService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.imageio.ImageIO

@Service
class EmailService {


    @Autowired
    private lateinit var eventStream: EventStream

    @Value("\${und.url.event}")
    private lateinit var eventApiUrl: String

    fun trackEmailRead(emailRead: EmailRead): EmailRead {
        toKafka(emailRead)
        return emailRead
    }

    private fun toKafka(emailRead: EmailRead): Boolean = eventStream.outEmailRead().send(MessageBuilder.withPayload(emailRead).build())

    fun getImage(id: String): ByteArray {
        try {
            //Get Client Id and Mongo Email Id
            val split = extractClientIdAndMongoEmailId(id)
            //Track the Email in Mongo DB
            trackEmailRead(EmailRead(split[0].toLong(), split[1]))

            //Create and Return a 1px image
            val bufferedImage = BufferedImage(1, 1,
                    BufferedImage.TYPE_INT_ARGB)
            val byteArrayOutputStream = ByteArrayOutputStream()
            ImageIO.write(bufferedImage, "jpg", byteArrayOutputStream)
            return byteArrayOutputStream.toByteArray()

        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun extractClientIdAndMongoEmailId(id: String): List<String> {
        //val code: String? = decrypt(URLDecoder.decode(id, "UTF-8"))
        //val split = code!!.split("###".toRegex(), 2)
        val split = id.split("&")
        val clientId = split[0]
        val mongoId = split[1].replace(".jpg", "")
        return Arrays.asList(clientId, mongoId)
    }

    fun getImageUrl(clientId: Int, mongoEmailId: String): String {
        val id = clientId.toString() + "###" + mongoEmailId
        return getImageUrl(id)
    }

    private fun getImageUrl(id: String): String {
        return "$eventApiUrl/email/image/${URLEncoder.encode(URLEncoder.encode(encrypt(id), "UTF-8"), "UTF-8")}.jpg"
    }
}