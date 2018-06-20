package com.und.service

import com.und.common.utils.*
import com.und.web.controller.exception.UndBusinessValidationException
import com.und.model.jpa.ClientVerification
import com.und.model.jpa.security.Authority
import com.und.model.jpa.security.AuthorityName
import com.und.web.controller.errorhandler.ValidationError
import com.und.web.model.RegistrationRequest
import com.und.model.utils.Email
import com.und.model.jpa.security.Client
import com.und.model.jpa.security.User
import com.und.service.security.AuthorityService
import com.und.service.security.ClientService
import com.und.security.utils.AuthenticationUtils
import com.und.web.controller.RegisterController
import com.und.web.controller.exception.UserAlreadyRegistered
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.mail.internet.InternetAddress

@Service
@Transactional
class RegistrationService {

    companion object {

        protected val logger = loggerFor(RegistrationService::class.java)
    }

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var clientService: ClientService

    @Autowired
    lateinit var emailService: EmailService

    @Autowired
    lateinit var authorityService: AuthorityService


    @Value("\${security.expiration}")
    private var expiration: Int = 0

    /**
     * validation should be part of transaction different from commit as it will
     * acquire lock on all table, even after this check non unique may exist, if database constraint is not followed.
     */
    fun validate(registrationRequest: RegistrationRequest) {
        val client = clientService.findByEmail(registrationRequest.email)
        val error = ValidationError()
        if (client != null) {
            val message = "email ${registrationRequest.email} is already registered"
            logger.error(message)
            throw  UserAlreadyRegistered(message)
        }
    }

    fun register(registrationRequest: RegistrationRequest): Client {
        val client = buildClient(registrationRequest)
        client.dateCreated = client.dateModified
        val adminUser = buildUser(registrationRequest, AuthenticationUtils.USER_TYPE_ADMIN)
        val eventUser = buildUser(registrationRequest, AuthenticationUtils.USER_TYPE_EVENT)
        client.addUser(adminUser)
        client.addUser(eventUser)
        //FIXME no need for keeping email code here
        client.clientVerification = buildClientVerificationEmail()
        return clientService.save(client)

    }

    fun update(registrationRequest: RegistrationRequest): Client {
        //TODO update details
        return Client()
    }

    private fun buildUser(registrationRequest: RegistrationRequest, userType: Int): User {
        val user = User()
        val roleUser:Authority = authorityService.findByName(AuthorityName.ROLE_USER) as Authority
        with(user) {
            email = registrationRequest.email
            password = passwordEncoder.encode(registrationRequest.password)
            firstname = registrationRequest.firstName
            lastname = registrationRequest.lastName

            mobile = registrationRequest.phone
            enabled = false
            lastPasswordResetDate = DateUtils().now()
            this.userType = userType
            username = usernameFromEmailAndType(email, this.userType)
            val authority = authorityService.authorityByType(this.userType)
            if (authority != null) authorities = arrayListOf(authority, roleUser)
            clientSecret = randomString(128)
        }
        return user
    }

    private fun buildClient(registrationRequest: RegistrationRequest): Client {
        val client = Client()
        with(client) {
            email = registrationRequest.email
            phone = registrationRequest.phone
            name = registrationRequest.name
            firstname = registrationRequest.firstName
            lastname = registrationRequest.lastName
            address = registrationRequest.address
            country = registrationRequest.country
            dateModified = DateUtils().now()
            //TODO add state
        }
        return client
    }

    private fun buildClientVerificationEmail(): ClientVerification {
        val clientVerification = ClientVerification()
        with(clientVerification) {
            emailCode = randomString(128)//UUID.randomUUID().toString()
            //this.client = client
            this.emailCodeDate = DateUtils().now()
        }
        return clientVerification
    }

    fun verifyEmail(email: String, code: String) {
        val client = clientService.findByEmail(email)
        if (client != null) {
            val codeMatch = client.clientVerification.emailCode == code
            val expired = DateUtils().now().time < client.clientVerification.emailCodeDate.time - expiration
            //FIXME convert exception to message wrapper
            when {
                codeMatch && !expired -> markAccountVerified(client)
                !codeMatch || expired -> {
                    val validationError = ValidationError()
                    validationError.addFieldError("emailVerification",
                            "Invalid Link, link has expired please request for new email")
                    throw UndBusinessValidationException(validationError)
                }

            }

        }

    }

    private fun markAccountVerified(client: Client) {
        client.emailVerified = true
        client.users.forEach { user -> user.enabled = true }
        clientService.save(client)
    }


    fun sendReVerificationEmail(email: String) {
        val client = clientByEmail(email)
        resetVerificationCode(client)
        sendVerificationEmail(client)
    }

    private fun resetVerificationCode(client: Client) {
        client.clientVerification = buildClientVerificationEmail()
        //TODO figure out a way to only update valid values
        clientService.save(client)
    }

    private fun clientByEmail(email: String): Client {
        val client = clientService.findByEmail(email)
        if (client == null) {
            logger.error("No user registered with email $email")
            logger.error("email $email is already verified")
            val validationError = ValidationError()
            validationError.addFieldError("Email Verification",
                    "This email is not registered , please register first.")
            throw UndBusinessValidationException(validationError)
        }
        if (client.emailVerified) {
            logger.error("email $email is already verified")
            val validationError = ValidationError()
            validationError.addFieldError("Email Verification",
                    "This email has already been verified")
            throw UndBusinessValidationException(validationError)

        }
        return client
    }


    fun sendVerificationEmail(client: Client) {

        emailService.sendVerificationEmail(client)
        logger.debug("email sent succesfully")

    }


}