package com.und.eventapi.validation

import java.util.regex.Pattern
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class LatitudeValidator : ConstraintValidator<ValidateLatitude, String> {

    override fun initialize(constraintAnnotation: ValidateLatitude) {}

    override fun isValid(latitude: String?, context: ConstraintValidatorContext?): Boolean {

        val result: Boolean
        result = if (latitude != null) {
            var regex="^-?([1-8]?[0-9][.]\\d{1,8}$|90[.]0{1,2}$)"
            val pattern = Pattern.compile(regex)
            val matcher = pattern.matcher(latitude)
            matcher.matches()
        } else true
        return result
    }
}
