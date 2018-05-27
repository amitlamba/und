package com.und.eventapi.validation

import java.time.LocalDate
import java.util.regex.Pattern
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class DateFormatValidator: ConstraintValidator<ValidateDateFormat, LocalDate> {
    override fun initialize(constraintAnnotation: ValidateDateFormat?) {}

    override fun isValid(date: LocalDate?, context: ConstraintValidatorContext?): Boolean {

        val result: Boolean
        result = if (date != null) {
            val pattern = Pattern.compile("(\\d{4})[-](0?[1-9]|1[012])[-](0?[1-9]|[12][0-9]|3[01])")
            val matcher = pattern.matcher(date.toString())
            matcher.matches()
        } else true
        return result
    }
}