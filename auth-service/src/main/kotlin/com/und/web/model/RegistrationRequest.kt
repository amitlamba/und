package com.und.web.model

import javax.validation.constraints.Email
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size


class RegistrationRequest {

    @NotNull
    @Email(message = "Email is not in valid format")
    lateinit var email: String

    @NotNull
    @Pattern(regexp = "^(?=\\S*[a-z])(?=\\S*[A-Z])(?=\\S*\\d)(?=\\S*[^\\w\\s])\\S{8,}\$",
            message = "password must contain one cap one small one number and one special")
    lateinit var password: String

    @NotNull
    @Size(min = 2, max = 30, message = "Name should contain 2 to 30 characters")
    lateinit var name: String

    @Size(min = 0, max = 20, message = "Country should contain 0 to 20 characters")
    var country: String? = null

    var address: String? = null

    @Size(min = 0, max = 20, message = "Phone should contain 0 to 20 characters")
    var phone: String? = null

    @Size(min = 0, max = 255, message = "First Name should contain 0 to 255 characters")
    var firstName: String? = null

    @Size(min = 0, max = 255, message = "Last Name should contain 0 to 255 characters")
    var lastName: String? = null

    var recaptchaToken: String? = null
}
