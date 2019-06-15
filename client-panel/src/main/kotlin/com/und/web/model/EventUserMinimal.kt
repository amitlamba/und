package com.und.web.model

import javax.validation.constraints.Email
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

open class EventUserMinimal {



    @Email
    open var undId: String? = null



    @Size(min = 10, max = 20)
    @Pattern(regexp = "[0-9]*")
    open var mobile: String? = null

    @Size(min = 2, max = 50)
    @Pattern(regexp = "[A-Za-z0-9][A-Za-z0-9\\s]*")
    open var firstName: String? = null

    @Size(min = 2, max = 50)
    @Pattern(regexp = "[A-Za-z0-9][A-Za-z0-9\\s]*")
    open var lastName: String? = null

    @Pattern(regexp = "[A-za-z]")
    open var gender: String? = null



    @Size(min = 4, max = 50)
    @Pattern(regexp = "[A-Za-z][A-Za-z\\s]*")
    open var country: String? = null


}


