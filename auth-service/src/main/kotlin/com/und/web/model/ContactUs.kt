package com.und.web.model

import com.und.web.controller.validation.ValidateContactUs
import javax.validation.constraints.Email
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

@ValidateContactUs
class ContactUs {

    var id: Long? = null

    @NotNull
    @Size(min = 2, max = 50)
    @Pattern(regexp = "[A-Za-z][a-zA-Z\\s]*")
    var name: String = ""

    @NotNull
    @Email
    var email: String = ""

    @NotNull
    @Size(min = 10, max = 20)
    @Pattern(regexp = "\\+?[0-9]*")
    var mobileNo: String = ""

    var message: String = ""

    var companyName: String = ""

}






