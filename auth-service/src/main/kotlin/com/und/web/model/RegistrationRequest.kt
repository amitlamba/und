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
    @Size(min = 4, max = 20, message = "Country should contain 4 to 20 characters")
    @Pattern(regexp = "[A-Za-z][A-Za-z\\s]*", message = "Country should contain alphabets and spaces only")
    var country: String? = null

    @NotNull
    @Pattern(regexp = "^#?[A-Za-z0-9/.:,\\-][A-Za-z0-9/.:,\\-\\s]*", message = "Address should contain alphabets, digits, spaces and #/.:,- special characters only")
    var address: String? = null

    @NotNull
    @Size(min = 10, max = 20, message = "Phone should contain 10 to 20 characters")
    @Pattern(regexp = "[0-9]*", message = "Phone number should contain digits only")
    var phone: String? = null

    @NotNull
    @Size(min = 1, max = 255, message = "First Name should contain 5 to 255 characters")
    @Pattern(regexp = "[A-Za-z]*", message = "First Name should contain alphabets only")
    lateinit var firstName: String

    @NotNull
    @Size(min = 1, max = 255, message = "Last Name should contain 5 to 255 characters")
    @Pattern(regexp = "[A-Za-z]*", message = "Last Name should contain alphabets only")
    lateinit var lastName: String

}
