package com.und.web.model

import javax.validation.constraints.Email
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

class ContactUs {

    var id: Long? = null

    @NotNull
    @Size(min = 2, max = 100)
    @Pattern(regexp = "[A-Za-z][a-zA-Z\\s]*")
    var name: String = ""

    @NotNull
    @Email()
    @Size(max = 200)
    var email: String = ""

    @NotNull
    @Size(min = 10, max = 15)
    @Pattern(regexp = "[0-9]*")
    var mobileNo: String = ""

    //FIXME It is wrong

    @Size(min = 50, max = 500)
    var message: String = ""

    @Size(min = 2, max = 500)
    var companyName: String = ""

}






