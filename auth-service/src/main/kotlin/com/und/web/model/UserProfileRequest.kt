package com.und.web.model

import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class UserProfileRequest(
        @Size(min = 0, max = 255)
        var firstname: String,

        @Size(min = 0, max = 255)
        var lastname: String,

        var address: String?,

        var phone: String?,

        val eventUserToken: String
)

data class PasswordRequest(
        @NotNull
        @Pattern(regexp = "^(?=\\S*[a-z])(?=\\S*[A-Z])(?=\\S*\\d)(?=\\S*[^\\w\\s])\\S{8,}\$",
                message = "password must contain one cap one small one number and one special")
        val password: String

)