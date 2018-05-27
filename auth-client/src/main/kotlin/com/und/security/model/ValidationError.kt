package com.und.security.model

import java.util.*

class ValidationError {

    private val fieldErrors = ArrayList<FieldError>()

    fun addFieldError(path: String, message: String) {
        val error = FieldError(path, message)
        fieldErrors.add(error)
    }

    fun getFieldErrors(): List<FieldError> {
        return fieldErrors
    }
}

class FieldError(val field: String, val message: String)