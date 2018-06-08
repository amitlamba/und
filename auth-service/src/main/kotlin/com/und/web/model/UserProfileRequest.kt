package com.und.web.model

import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class UserProfileRequest(

        @Size(min = 1, max = 50)
        @Pattern(regexp = "[A-Za-z][a-zA-Z\\s]*")
        var firstname: String,

        @Size(min = 1, max = 50)
        @Pattern(regexp = "[A-Za-z][a-zA-Z\\s]*")
        var lastname: String,

        @Size(min = 2, max = 255)
        var address: String?,

        @Size(min = 10, max = 20)
        var phone: String?,

        val eventUserToken: String
)

data class PasswordRequest(
        @NotNull
        @Pattern(regexp = "^(?=\\S*[a-z])(?=\\S*[A-Z])(?=\\S*\\d)(?=\\S*[^\\w\\s])\\S{8,}\$",
                message = "password must contain one cap one small one number and one special")
        val password: String

)