package com.und.web.controller.errorhandler

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.und.common.utils.loggerFor
import com.und.web.controller.exception.EventUserNotFoundException
import com.und.web.controller.exception.UndBusinessValidationException

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.lang.Exception
import java.util.*


@RestControllerAdvice
class RestErrorHandler : ResponseEntityExceptionHandler() {

    companion object {

        protected val logger: Logger = loggerFor(RestErrorHandler::class.java)
    }

    @Autowired
    lateinit var messageSource: MessageSource


    override fun handleBindException(ex: BindException, headers: HttpHeaders, status: HttpStatus, request: WebRequest): ResponseEntity<Any> {
        logger.error("400 Status Code", ex)
        val result = ex.bindingResult
        val fieldErrors = result.fieldErrors
        val errors = formatFieldErrors(request.locale, fieldErrors)
        return handleExceptionInternal(ex, errors, HttpHeaders(), HttpStatus.BAD_REQUEST, request)
    }

    private fun formatFieldErrors(locale: Locale, fieldErrors: List<org.springframework.validation.FieldError>): List<FieldError> {
        return fieldErrors
                .filter {it.defaultMessage != null }
                .map {
                    FieldError(field = it.field,
                            message = messageSource.getMessage(it, locale)
                    )
                }

    }


    //@ExceptionHandler(MethodArgumentNotValidException::class)
    //@ResponseStatus(HttpStatus.BAD_REQUEST)
    override fun handleMethodArgumentNotValid(ex: MethodArgumentNotValidException, headers: HttpHeaders, status: HttpStatus, request: WebRequest): ResponseEntity<Any> {
        logger.error("400 Status Code", ex)
        val result = ex.bindingResult
        val fieldErrors = result.fieldErrors
        val errors = formatFieldErrors(request.locale, fieldErrors)
        return handleExceptionInternal(ex, errors, HttpHeaders(), HttpStatus.BAD_REQUEST, request)
    }

    @ExceptionHandler(InvalidFormatException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleReCaptchaInvalid(ex: RuntimeException, request: WebRequest): ResponseEntity<Any> {
        logger.error("400 Status Code", ex)
        val bodyOfResponse = GenericResponse(messageSource.getMessage("message.invalidJSON", null, request.locale), "InvalidJson")
        return handleExceptionInternal(ex, bodyOfResponse, HttpHeaders(), HttpStatus.BAD_REQUEST, request)
    }


    @ExceptionHandler(HttpMessageNotReadableException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleReCaptchaUnavailable(ex: RuntimeException, request: WebRequest): ResponseEntity<Any> {
        logger.error("500 Status Code", ex)
        val bodyOfResponse = GenericResponse(messageSource.getMessage("message.unreadable.http", null, request.locale), "InvalidHTTPMessage")
        return handleExceptionInternal(ex, bodyOfResponse, HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request)
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleInternal(ex: RuntimeException, request: WebRequest): ResponseEntity<Any> {
        logger.error("500 Status Code", ex)
        val bodyOfResponse = GenericResponse(messageSource.getMessage("message.error", null, request.locale), "InternalError")
        return ResponseEntity(bodyOfResponse, HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(UndBusinessValidationException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    fun businessValidationError(ex: UndBusinessValidationException, request: WebRequest): ResponseEntity<Any> {
        logger.error("400 Status Code", ex)
        val bodyOfResponse = GenericResponse(messageSource.getMessage("message.error", null, request.locale), ex.localizedMessage)
        return ResponseEntity(bodyOfResponse, HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    fun processAuthError(ex: org.springframework.security.access.AccessDeniedException, request: WebRequest): ResponseEntity<Any> {
        logger.error("401 Status Code", ex)
        val bodyOfResponse = GenericResponse(messageSource.getMessage("message.accessDenied", null, request.locale), ex.localizedMessage)
        return ResponseEntity(bodyOfResponse, HttpHeaders(), HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(UsernameNotFoundException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    fun processAuthError(ex: UsernameNotFoundException, request: WebRequest): ResponseEntity<Any> {
        logger.error("401 Status Code", ex)
        val bodyOfResponse = GenericResponse(messageSource.getMessage("message.usernameNotFound", null, request.locale), ex.localizedMessage)
        return ResponseEntity(bodyOfResponse, HttpHeaders(), HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(EventUserNotFoundException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun eventUser(ex: EventUserNotFoundException, request: WebRequest): ResponseEntity<Any> {
        logger.error("401 Status Code", ex)
        val bodyOfResponse = GenericResponse(messageSource.getMessage("message.eventUserNotFound", null, request.locale), ex.localizedMessage)
        return ResponseEntity(bodyOfResponse, HttpHeaders(), HttpStatus.UNAUTHORIZED)
    }






}
