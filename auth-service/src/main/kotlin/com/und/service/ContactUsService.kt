package com.und.service

import com.und.common.utils.loggerFor
import com.und.model.utils.Email
import com.und.model.jpa.ContactUs
import com.und.repository.jpa.ContactUsRepository
import com.und.web.controller.errorhandler.ValidationError
import com.und.web.controller.exception.EmailAlreadyRegisteredException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.mail.internet.InternetAddress
import com.und.web.model.ContactUs as WebContactUs

@Service
class ContactUsService {

    companion object {

        protected val logger = loggerFor(ContactUsService::class.java)
    }

    @Autowired
    private lateinit var contactUsRepository: ContactUsRepository

    @Autowired
    private lateinit var emailService: EmailService

    fun save(webContactUs: WebContactUs) {
        val startDate = LocalDateTime.now().minusHours(24)
        val endDate = LocalDateTime.now()
        val contactUsEmail = contactUsRepository
                .findByEmailBetweenDates(webContactUs.email, startDate, endDate)
        if (contactUsEmail.isPresent && contactUsEmail.get().size >= 2) {
            val error = ValidationError()
            error.addFieldError("email", "Email is already registered for contact")
            throw EmailAlreadyRegisteredException(error)
        } else {
            val contactUs = buildContactUs(webContactUs)
            val persistedContactUs = contactUsRepository.save(contactUs)
            webContactUs.id = persistedContactUs.id
            sendContactUsEmail(contactUs)
        }
    }


    fun buildContactUs(webContactUs: WebContactUs): ContactUs {

        val contactUS = ContactUs()
        contactUS.name = webContactUs.name
        contactUS.email = webContactUs.email
        contactUS.mobileNo = webContactUs.mobileNo
        contactUS.message = webContactUs.message
        contactUS.companyName = webContactUs.companyName

        return contactUS
    }


    fun sendContactUsEmail(contactInfo: ContactUs) {
        fun buildMesageBody(): String {
            return """
                    Dear ${contactInfo.name},
                        Thanks for contacting us
                        we will connect with you soon!

                    Thanks
                    Team UND
               """.trimIndent()
        }

        val message = Email(
                clientID = 1,
                emailSubject = "Your Request Accepted",
                fromEmailAddress = InternetAddress("admin@userndot.com", "UserNDot Admin"),
                toEmailAddresses = arrayOf(InternetAddress(contactInfo.email, contactInfo.name)),
                emailBody = buildMesageBody()

        )
        emailService.sendEmail(message)
        logger.debug("email sent successfully")

    }

}