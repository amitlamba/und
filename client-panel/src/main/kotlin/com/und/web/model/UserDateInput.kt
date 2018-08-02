package com.und.web.model

import com.und.web.validation.DateValidator
import javax.validation.constraints.Pattern

class UserDateInput {

    @DateValidator(message = "Invalid Date")
    var fromDate:String?=null
    @DateValidator(message = "Invalid Date")
    var toDate:String?=null
}