package com.und.web.validation

import com.und.common.utils.loggerFor
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class DateValidatorClass:ConstraintValidator<DateValidator,String> {
    companion object {
        var logger= loggerFor(DateValidatorClass::class.java)
    }

    override fun initialize(constraintAnnotation: DateValidator?) {

    }
    override fun isValid(date: String?, context: ConstraintValidatorContext?): Boolean {
        if(date == null)return true
        var isValidDate:Boolean=false
        var pattern: Pattern = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$")
        if (pattern.matcher(date).matches()) {
            try {
                val format: SimpleDateFormat = SimpleDateFormat("yyyy-MM-dd")
                format.isLenient = false
                var newFromDate: Date = format.parse(date)
                isValidDate=true

            }catch (e :Exception){
               logger.debug("User enter invalid date or dateformat")
            }
        }
        return isValidDate
    }
}