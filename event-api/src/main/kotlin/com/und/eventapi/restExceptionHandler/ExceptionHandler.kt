package com.und.eventapi.restExceptionHandler

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.lang.Exception

@RestControllerAdvice
class ExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    fun handleFieldErrors(ex: MethodArgumentNotValidException): ErrorList {

        var errorList: List<FieldError> = listOf()
        val result = ex.bindingResult
        val fieldErrors = result.fieldErrors
        fieldErrors
                .filter { it != null && it.defaultMessage != null }
                .forEach {
                    errorList = listOf(
                            FieldError(field = it.field,
                                    message = it.defaultMessage)
                    )
                }
        return ErrorList(errorList)
    }

   /* @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    fun processOtherError(ex: Exception): Error {
        val error = Error("INTERNAL SERVER ERROR")
        return error
    }*/

    @ExceptionHandler(AccessDeniedException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    fun handleAccessDeniedException(ex: AccessDeniedException): Error {
        //TODO Handle AccessDenied Exception
        val error = Error("Unauthorized")
        return error
    }

    @ExceptionHandler(UsernameNotFoundException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    fun processAuthError(ex: Exception) {
        //TODO handle UsernameNotFound Exception

    }

    @ExceptionHandler(InvalidFormatException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    fun handleInvalidFormatException(ex: Exception) :Error{
        val error=Error("InvalidFormatException")
        return error
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ResponseBody
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException) :Error{
        val error=Error("HttpMessageNotReadableException")
        return error
    }
}










