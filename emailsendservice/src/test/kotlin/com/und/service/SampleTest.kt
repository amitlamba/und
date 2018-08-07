package com.und.service

import com.und.model.utils.Email
import com.und.model.utils.EmailSMTPConfig
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import javax.mail.internet.InternetAddress

//http://websystique.com/spring/spring-4-email-using-velocity-freemaker-template-library/

@RunWith(SpringRunner::class)
@SpringBootTest
class SampleTest {
    // Replace sender@example.com with your "From" address.
// This address must be verified.
    val FROM = "amit@userndot.com"
    val FROMNAME = "Amit from Userndot"

    // Replace recipient@example.com with a "To" address. If your account
// is still in the sandbox, this address must be verified.
    val TO = "shivprataps@gmail.com"

    // Replace smtp_username with your Amazon SES SMTP user name.
    val SMTP_USERNAME = "smtp_username"

    // Replace smtp_password with your Amazon SES SMTP password.
    val SMTP_PASSWORD = "smtp_password"

    // The name of the Configuration Set to use for this message.
// If you comment out or remove this variable, you will also need to
// comment out or remove the header below.
    val CONFIGSET = "ConfigSet"

    // Amazon SES SMTP host name. This example uses the US West (Oregon) Region.
    val HOST = "email-smtp.us-west-2.amazonaws.com"

    // The port you will connect to on the Amazon SES SMTP endpoint.
    val PORT = 587

    val SUBJECT = "Amazon SES test (SMTP interface accessed using Java)"

    val BODY = arrayOf(
            "<h1>Amazon SES SMTP Email Test</h1>",
            "<p>This email was sent with Amazon SES using the ",
            "<a href='https://github.com/javaee/javamail'>Javamail Package</a>",
            " for <a href='https://www.java.com'>Java</a>.").joinToString(
            System.getProperty("line.separator"))

    @Autowired
    private lateinit var emailSendService: EmailSendService

    @Test
    fun testEmailSend() {
        val email = Email(clientID = 2,
                fromEmailAddress = InternetAddress(FROM, FROMNAME),
                toEmailAddresses = arrayOf(InternetAddress(TO)),
                emailSubject = SUBJECT,
                emailBody = BODY,
                emailTemplateId = 0L,
                emailTemplateName = "",
                campaignId = 0
                )
        val emailSMTPConfig = EmailSMTPConfig(null, 1, HOST, PORT, SMTP_USERNAME, SMTP_PASSWORD, CONFIGSET)

        emailSendService.sendEmailBySMTP(emailSMTPConfig, email)
    }

    @Test
    fun testSesSMTPEmailSend() {
        val email: Email = Email(clientID = 1,
                fromEmailAddress = InternetAddress("amit@userndot.com", "Amit from Userndot"),
                toEmailAddresses = arrayOf(InternetAddress("amitlamba4198@gmail.com")),
                emailSubject = SUBJECT,
                emailBody = BODY,
                emailTemplateId = 0,
                emailTemplateName = "",
                campaignId = 0)
        val emailSMTPConfig = EmailSMTPConfig(null, 1, "email-smtp.us-east-1.amazonaws.com", 587, "AKIAIS6IJSVKWL7VUIIQ", "AlEf0RBhmCMDcTuwDDrl9BonxawtKZrPC2b4Mtn4o2v4", null)

        emailSendService.sendEmailBySMTP(emailSMTPConfig, email)
    }

    @Autowired
    private lateinit var cacheService: CacheService

    @Test
    fun testCaching() {
        var lastString: String = "0"
        for(i in 1..100) {
            var currentString = cacheService.cachingFunction(1)
            println("Current String: ${currentString}, Last String: ${lastString}")
            println(Math.random().toString())
            lastString = currentString
            Thread.sleep(100)
        }
    }

}
