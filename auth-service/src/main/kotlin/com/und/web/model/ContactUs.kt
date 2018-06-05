package com.und.web.model

import javax.validation.constraints.Email
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

class ContactUs {

    var id: Long? = null

    @Size(min = 2, max = 100, message = "{contactUS.name.invalidSize}")
    @Pattern(regexp = "[A-Za-z][a-zA-Z\\s]*", message = "{contactUS.name.invalid}")
    var name: String = ""

    @Email(message = "{contactUS.email.invalid}")
    var email: String = ""

    @Size(min = 10, max = 15, message = "{contactUS.mobileNo.invalidSize}")
    @Pattern(regexp = "[0-9]*", message = "{contactUS.mobileNo.invalidDigits}")
    var mobileNo: String = ""

    @Size(min = 50, max = 500, message = "{contactUS.message.invalidSize}")
    var message: String = ""

    @Size(min = 2, max = 200, message = "{contactUS.companyName.invalidSize}")
    var companyName: String = ""

}






