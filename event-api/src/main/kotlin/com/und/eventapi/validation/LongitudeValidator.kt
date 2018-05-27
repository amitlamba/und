package com.und.eventapi.validation

import java.util.regex.Pattern
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class LongitudeValidator : ConstraintValidator<ValidateLongitude, String> {

    override fun initialize(longitude: ValidateLongitude) {}

    override fun isValid(longitude: String?, context: ConstraintValidatorContext?): Boolean {

        val result: Boolean
        result = if (longitude != null) {
            val pattern= Pattern.compile ("^-?([1]?[1-7][1-9]|[1]?[1-8][0]|[1-9]?[0-9])[.]\\d{2}")
            val matcher = pattern.matcher(longitude)
            matcher.matches()
        } else true
        return result
    }
}



