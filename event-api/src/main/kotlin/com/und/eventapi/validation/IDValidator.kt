package com.und.eventapi.validation

import java.util.regex.Pattern
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class IDValidator : ConstraintValidator<ValidateID, Int> {

    override fun initialize(constraintAnnotation: ValidateID) {}

    override fun isValid(id: Int?, context: ConstraintValidatorContext?): Boolean {

        val result: Boolean
        result = if (id != null) {
            val pattern = Pattern.compile("-1|[0-9]+")
            val matcher = pattern.matcher(id.toString())
            matcher.matches()
        } else true
        return result
    }
}