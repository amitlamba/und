package com.und.service

import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import com.und.config.EventStream
import com.und.model.utils.Email
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.MessageBuilder
import org.springframework.test.util.ReflectionTestUtils
import javax.mail.internet.InternetAddress


@Ignore
class EmailServiceTest {

    @InjectMocks
    private lateinit var emailService: EmailService

    @Mock
    private lateinit var eventStream: EventStream

    @Mock
    private lateinit var messageChannel: MessageChannel


    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        ReflectionTestUtils.setField(emailService, "eventStream", eventStream)

        whenever(eventStream.clientEmailSend()).thenReturn(messageChannel)

    }

    @Test
    fun sendEmailTest() {
/*        val email = Email(
                clientID = 1,
                fromEmailAddress = InternetAddress("amit@userndot.com", "UserNDot Admin"),
                toEmailAddresses = arrayOf(InternetAddress("amitlamba4198@gmail.com")),
                emailBody = """
                    Hi, $//userName
                    please click http://localhost:8080/register/resetpassword/amitlamba4198@gmail.com/this-is-the-code to reset password
                """.trimIndent(),
                emailSubject = "forgot password")

        whenever(messageChannel.send(MessageBuilder.withPayload(email).build())).thenReturn(true)
        emailService.sendEmail(email)
        verify(eventStream, times(1)).clientEmailSend()*/
    }
}