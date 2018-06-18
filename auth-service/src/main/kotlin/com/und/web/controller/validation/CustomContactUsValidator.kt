package com.und.web.controller.validation

import com.und.web.controller.exception.InvalidContactUsCompanyNameException
import com.und.web.controller.exception.InvalidContactUsException
import com.und.web.controller.exception.InvalidContactUsMessageException
import com.und.web.model.ContactUs
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class CustomContactUsValidator : ConstraintValidator<ValidateContactUs, ContactUs> {

    override fun initialize(String: ValidateContactUs) {}

    override fun isValid(contactUs: ContactUs, context: ConstraintValidatorContext?): Boolean {

        return if (contactUs.companyName == "" && contactUs.message != "") {
            if (contactUs.message.length !in 49..501)
                throw InvalidContactUsMessageException("Message should be greater than 50 and less than 500 characters")
            else true
        } else if (contactUs.message == "" && contactUs.companyName != "") {
            if (contactUs.companyName.length !in 1..201)
                throw InvalidContactUsCompanyNameException("Company name should be greater than 1 and less than 200 characters")
            else true
        } else if(contactUs.message !== "" && contactUs.companyName != ""){
            if (contactUs.message.length !in 49..501)
                throw InvalidContactUsMessageException("Message should be greater than 50 and less than 500 characters")
            if (contactUs.companyName.length !in 1..201)
                throw InvalidContactUsCompanyNameException("Company name should be greater than 1 and less than 200 characters")
            else true
        }
        else throw InvalidContactUsException("Invalid Contact us")
    }
}

