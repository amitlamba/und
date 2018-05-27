package com.und.eventapi.validation

import java.util.*
import java.util.Currency.getAvailableCurrencies
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class CurrencyValidator: ConstraintValidator<ValidateCurrency, String>{

    var currencySet = getAvailableCurrencies()

    override fun initialize(currency: ValidateCurrency) {}

    override fun isValid(currency: String?, context: ConstraintValidatorContext?): Boolean {

        val currencyCode:Currency =Currency.getInstance(currency)
        return currencySet.contains(currencyCode)
    }
}