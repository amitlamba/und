package com.und

import com.und.config.EventStream
import com.und.service.EmailService
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.test.util.ReflectionTestUtils
import java.net.URLDecoder

class EmailServiceTest {

    @InjectMocks
    private lateinit var emailService: EmailService

    @Mock
    private lateinit var eventStream: EventStream

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        ReflectionTestUtils.setField(emailService, "eventApiUrl", "https://userndot.com/event")
        ReflectionTestUtils.setField(emailService, "eventStream", eventStream)
    }

    @Test
    fun testEmailUrlCreation() {
        val img = emailService.getImageUrl(1, "abc")
        println(img)
        val id = img.split("/")[6].split(".")[0]
        val decoded = emailService.extractClientIdAndMongoEmailId(URLDecoder.decode(id, "UTF-8"))
        println(decoded)

    }
}