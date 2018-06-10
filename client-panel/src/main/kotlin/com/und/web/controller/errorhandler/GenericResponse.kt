package com.und.web.controller.errorhandler

import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError

class GenericResponse(var message: String?, var error: String? = null) {


    constructor(allErrors: List<ObjectError>, error: String) : this(null, error) {
        val temp = allErrors.map { e ->
            if (e is FieldError) {
                """{"field": "${e.field}","defaultMessage":"${e.defaultMessage}"}"""

            } else {
                """{"object": "${e.objectName}","defaultMessage":"${e.defaultMessage}"}"""
            }
        }.joinToString(",")


        this.message = "[$temp]"
    }

}
