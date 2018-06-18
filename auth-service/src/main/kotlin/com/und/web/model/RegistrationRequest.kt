package com.und.web.model

import com.fasterxml.jackson.annotation.JsonProperty
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

    @NotNull
    @Size(min = 4, max = 60, message = "Country should contain 4 to 60 characters")
    var country: String? = null

    @NotNull
    @Size(min = 2, max = 255, message = "Address should contain 2 to 255 characters")
    var address: String? = null

    @NotNull
    @Size(min = 10, max = 20, message = "Phone should contain 10 to 20 characters")
    var phone: String? = null

    @NotNull
    @Size(min = 1, max = 50, message = "First Name should contain 1 to 50 characters")
    @Pattern(regexp = "[A-Za-z][a-zA-Z\\s]*")
    lateinit var firstName: String

    @NotNull
    @Size(min = 1, max = 50, message = "Last Name should contain 1 to 50 characters")
    @Pattern(regexp = "[A-Za-z][a-zA-Z\\s]*")
    lateinit var lastName: String

}
