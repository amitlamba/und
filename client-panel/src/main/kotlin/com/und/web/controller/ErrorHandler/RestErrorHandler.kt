package com.und.web.controller.ErrorHandler

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.und.common.utils.loggerFor
import com.und.eventapi.restExceptionHandler.ErrorList
import com.und.exception.UndBusinessValidationException
import com.und.web.model.ValidationError
import org.hibernate.JDBCException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.lang.Exception
import javax.naming.AuthenticationException


@RestControllerAdvice
class RestErrorHandler {

    companion object {

        protected val logger = loggerFor(RestErrorHandler::class.java)
    }


    @ExceptionHandler(JDBCException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    fun processOtherError(ex: Exception): String {
        logger.debug("Handling INTERNAL SEREVR error")
        logger.error("error occured", ex)
        return "Something Wrong happened Try after some time!"

    }

    @ExceptionHandler(AccessDeniedException::class, AuthenticationException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    fun processAuthEroor(ex: Exception): String {
        logger.debug("Handling INTERNAL SERVER error")
        logger.error("error occurred", ex)
        return "Access Denied!"

    }


    @ExceptionHandler(InvalidFormatException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    fun handleInvalidFormatException(ex: Exception): String {
        logger.debug("Handling INTERNAL SEREVR error")
        logger.error("error occured", ex)
        return "Invalid access token"

    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ResponseBody
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException): String {
        logger.debug("Handling INTERNAL SEREVR error")
        logger.error("error occured", ex)
        return "Invalid data sent"
    }

    @ExceptionHandler(UndBusinessValidationException::class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ResponseBody
    fun businessValidationError(ex: UndBusinessValidationException): ValidationError {
        logger.debug("Handling INTERNAL SERVER ERROR")
        logger.error("error occurred", ex)
        return ex.error

    }


    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    fun handleFieldErrors(ex: MethodArgumentNotValidException): ErrorList {

        var errorList: List<com.und.eventapi.restExceptionHandler.FieldError> = listOf()
        val result = ex.bindingResult
        val fieldErrors = result.fieldErrors
        fieldErrors
                .filter { it != null && it.defaultMessage != null }
                .forEach {
                    errorList = listOf(
                            com.und.eventapi.restExceptionHandler.FieldError(field = it.field,
                                    message = it.defaultMessage)
                    )
                }
        return ErrorList(errorList)
    }

/*    private fun processFieldErrors(fieldErrors: List<FieldError>): ValidationError {
        val dto = ValidationError()
        for (fieldError in fieldErrors) {
            val localizedErrorMessage = resolveLocalizedErrorMessage(fieldError)
            logger.debug("Adding error message: {} to field: {}", localizedErrorMessage, fieldError.field)
            dto.addFieldError(fieldError.field, localizedErrorMessage)
        }
        return dto
    }

    private fun resolveLocalizedErrorMessage(fieldError: FieldError): String {
        //TODO FIXME error message to be picked from localised files
        val currentLocale = LocaleContextHolder.getLocale()
        var localizedErrorMessage = messageSource.getMessage(fieldError, currentLocale)

        //If a message was not found, return the most accurate field error code instead.
        //You can remove this check if you prefer to get the default error message.
        if (localizedErrorMessage == fieldError.defaultMessage) {
            val fieldErrorCodes = fieldError.codes
            if(fieldErrorCodes != null) {
                localizedErrorMessage = fieldErrorCodes[0] ?: ""
            }
        }

        return localizedErrorMessage
    }*/
}
