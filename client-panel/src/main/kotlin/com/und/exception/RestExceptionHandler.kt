package com.und.exception

import com.und.error.ErrorDetails
import org.apache.kafka.common.errors.InvalidRequestException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(InvalidRequestException::class)
    @ResponseStatus(HttpStatus.OK)
    fun handleRestExceptions(ex: InvalidRequestException):ErrorDetails {

        val message=ex.message
        return ErrorDetails(message)

    }
}