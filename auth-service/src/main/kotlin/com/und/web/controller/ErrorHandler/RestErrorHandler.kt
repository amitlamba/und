package com.und.web.controller.ErrorHandler

import com.und.common.utils.loggerFor
import com.und.exception.UndBusinessValidationException
import com.und.web.model.ValidationError
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureException
import io.jsonwebtoken.UnsupportedJwtException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpStatus
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*
import java.lang.Exception


@RestControllerAdvice
class RestErrorHandler {

    companion object {

        protected val logger = loggerFor(RestErrorHandler::class.java)
    }

    @Autowired
    lateinit var messageSource: MessageSource

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    fun processOtherError(ex: Exception) {
        logger.debug("Handling INTERNAL SEREVR error")
        logger.error("error occured",ex)

    }

    @ExceptionHandler(UndBusinessValidationException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    fun businessValidationError(ex: UndBusinessValidationException) : ValidationError {
        logger.debug("Handling INTERNAL SEREVR error")
        logger.error("error occured",ex)
        return ex.error

    }

    @ExceptionHandler(AccessDeniedException::class, UsernameNotFoundException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    fun processAuthEroor(ex: Exception):String {
        logger.debug("Handling INTERNAL SEREVR error")
        logger.error("error occured",ex)
        return "hello"

    }

    @ExceptionHandler(MalformedJwtException::class, SignatureException::class,UnsupportedJwtException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    fun processAuthEroorJwt(ex: Exception):String {
        logger.debug("Handling INTERNAL SEREVR error")
        logger.error("error occured",ex)
        return "hello"

    }



    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun processValidationError(ex: MethodArgumentNotValidException): ValidationError {
        logger.debug("Handling form validation error")

        val result = ex.bindingResult
        val fieldErrors = result.fieldErrors

        return processFieldErrors(fieldErrors)
    }

    private fun processFieldErrors(fieldErrors: List<FieldError>): ValidationError {
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
            localizedErrorMessage = fieldErrorCodes[0]?:""
        }

        return localizedErrorMessage
    }
}
