package com.und.service

import com.und.model.utils.Email
import com.und.model.utils.EmailSMTPConfig
import org.junit.Before
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.MockitoAnnotations
import org.springframework.test.util.ReflectionTestUtils
import java.util.regex.Pattern
import javax.mail.internet.InternetAddress

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
        val clientId = 1L
        val mongoEmailId = "accdef"
        val content1 = emailSendService.insertUnsubscribeLinkIfApplicable(content, unsubscribeLink1, clientId, mongoEmailId)
        val finalContent1 = """
            <html>
            <head></head>
            <body><div><a href="https://und.com/unsubscribe?d=2&c=1&e=accdef">Unsibscribe</a></div></body>
            </html>
            """
        assert(content1 == finalContent1)
        val content2 = emailSendService.insertUnsubscribeLinkIfApplicable(content, unsubscribeLink2, clientId, mongoEmailId)
        val finalContent2 = """
            <html>
            <head></head>
            <body><div><a href="https://und.com/unsubscribe?c=1&e=accdef">Unsibscribe</a></div></body>
            </html>
            """
        assert(content2 == finalContent2)
    }

    @Test
    fun getVariableFromTemplate(){
        var emailSubject = "\${user.identity.firstName} hello \${user.identity.lastName}"
        val emailBody = "\${user.identity.firstName}"
        val listOfVariable = mutableSetOf<String>()
        val pattern = Pattern.compile("(?<group>\\\$\\{.*?\\})")
        val subjectMatcher = pattern.matcher(emailSubject)
        val bodyMatcher = pattern.matcher(emailBody)
        val subjectGroupMatch = subjectMatcher.groupCount()
        val bodyGroupMatch = bodyMatcher.groupCount()
        println("No fo variable in subject $subjectGroupMatch")
        println("No of variable in body $bodyGroupMatch")
        if(subjectMatcher.matches()){
            for (i in 0..(subjectGroupMatch - 1) step 1) {
                listOfVariable.add(subjectMatcher.group(i + 1))
            }
        }
        if (bodyMatcher.matches()) {
            for (i in 0..(bodyGroupMatch - 1) step 1) {
                listOfVariable.add(bodyMatcher.group(i + 1))
            }
        }
        println("list Of variables $listOfVariable")
    }


}