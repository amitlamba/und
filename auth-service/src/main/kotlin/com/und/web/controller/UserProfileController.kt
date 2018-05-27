package com.und.web.controller

import com.und.common.utils.loggerFor
import com.und.web.model.Data
import com.und.web.model.Response
import com.und.web.model.ResponseStatus
import com.und.web.model.PasswordRequest
import com.und.web.model.UserProfileRequest
import com.und.model.jpa.security.Client
import com.und.service.security.ClientService
import com.und.service.security.SecurityAuthenticationResponse
import com.und.service.security.UserService
import com.und.security.utils.AuthenticationUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@CrossOrigin(origins= ["*"], maxAge = 3600)
@RestController
@RequestMapping("/setting")
class UserProfileController {

    companion object {

        protected val logger = loggerFor(UserProfileController::class.java)
    }

    @Autowired
    lateinit var userService: UserService


    @Autowired
    lateinit var clientService: ClientService


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = ["/resetpassword"])
    fun resetPassword(@RequestBody @Valid passwordRequest: PasswordRequest): ResponseEntity<Response> {
        val userDetails = AuthenticationUtils.principal

        userService.resetPassword(userDetails, passwordRequest.password)
        return ResponseEntity.ok().body(Response(
                status = ResponseStatus.SUCCESS,
                message = "Password successfully changed"
        ))
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping(value = ["/userDetails"])
    fun userDetails(): ResponseEntity<Response> {
        val clientId = AuthenticationUtils.clientID
        return if (clientId != null) {
            val client = clientService.findById(clientId)
            val userProfile = UserProfileRequest(
                    firstname = client.firstname ?: "",
                    lastname = client.lastname ?: "",
                    address = client.address,
                    phone = client.phone,
                    eventUserToken = client.users.filter { it.userType == AuthenticationUtils.USER_TYPE_EVENT }.first().key
                            ?: ""

            )

            ResponseEntity.ok().body(Response(
                    status = ResponseStatus.SUCCESS,
                    data = Data(userProfile)
            ))
        } else {
            ResponseEntity.badRequest().body(Response(
                    status = ResponseStatus.ERROR,
                    message = "not a valid login"
            ))
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = ["/updateUserDetails"])
    fun updateUserDetails(@Valid @RequestBody request: UserProfileRequest): ResponseEntity<Response> {
        val client = Client()
        with(client) {
            id = AuthenticationUtils.clientID
            firstname = request.firstname
            lastname = request.lastname
            address = request.address
            phone = request.phone
        }
        val status = clientService.updateClient(client)
        return if (status) ResponseEntity.ok().body(Response()) else ResponseEntity.badRequest().body(Response())
    }


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = ["/refreshToken/{new}"])
    fun generateToken(@PathVariable("new") new:Boolean ): ResponseEntity<*> {
        //val device = DeviceUtils.getCurrentDevice(request)
        val user = AuthenticationUtils.principal
        val jwt  = if(new) {
             userService.updateJwtOfEventUser(user)
        } else userService.retrieveJwtOfEventUser( user)
        return  ResponseEntity.ok(
                Response(
                        status = ResponseStatus.SUCCESS,
                        data = Data(SecurityAuthenticationResponse(jwt.loginKey))
                )
        )
    }



}