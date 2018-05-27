package com.und.eventapi.validation

import com.und.web.model.eventapi.Event
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class DateValidator : ConstraintValidator<ValidateDate, Event> {

    override fun initialize(date: ValidateDate) {}

    override fun isValid(event: Event, context: ConstraintValidatorContext?): Boolean {

        val result: Boolean

        result = if (event.getDateStart() != null && event.getDateEnd() != null) {
            event.getDateStart()!! < event.getDateEnd()
            //var bindingResult:BindingResult
            //throw MethodArgumentNotValidException()
        } else true
        return result
    }
}


