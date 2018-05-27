package com.und.service

import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.MockitoAnnotations
import org.springframework.test.util.ReflectionTestUtils

class EmailSendServiceTest {

    @InjectMocks
    private lateinit var emailSendService: EmailHelperService

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        ReflectionTestUtils.setField(emailSendService, "eventApiUrl", "https://userndot.com/event")
    }

    @Test
    fun insertUnsubscribeLinkIfApplicableTest() {
        var content = """
            <html>
            <head></head>
            <body><div><a href="##UND_UNSUBSCRIBE_LINK##">Unsibscribe</a></div></body>
            </html>
            """
        val unsubscribeLink1 = "https://und.com/unsubscribe?d=2"
        val unsubscribeLink2 = "https://und.com/unsubscribe"
        val clinetId = 1
        val mongoEmailId = "accdef"
        val content1 = emailSendService.insertUnsubscribeLinkIfApplicable(content, unsubscribeLink1, clinetId, mongoEmailId)
        val finalContent1 = """
            <html>
            <head></head>
            <body><div><a href="https://und.com/unsubscribe?d=2&c=1&e=accdef">Unsibscribe</a></div></body>
            </html>
            """
        assert(content1 == finalContent1)
        val content2 = emailSendService.insertUnsubscribeLinkIfApplicable(content, unsubscribeLink2, clinetId, mongoEmailId)
        val finalContent2 = """
            <html>
            <head></head>
            <body><div><a href="https://und.com/unsubscribe?c=1&e=accdef">Unsibscribe</a></div></body>
            </html>
            """
        assert(content2 == finalContent2)
    }

}