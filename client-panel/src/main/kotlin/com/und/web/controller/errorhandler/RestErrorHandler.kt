package com.und.web.controller.errorhandler

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.und.common.utils.loggerFor
import com.und.web.controller.exception.*
import com.und.web.controller.exception.EmailTemplateDuplicateNameException
import com.und.web.controller.exception.EventUserNotFoundException
import com.und.web.controller.exception.SegmentNotFoundException
import com.und.web.controller.exception.UndBusinessValidationException

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.MessageSource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
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


/*    @ExceptionHandler(InputUserDateFormatException::class)
    fun handleInputUserDateFormatException(ex: InputUserDateFormatException):ResponseEntity<ReportError>{
        logger.error(HttpStatus.BAD_REQUEST,ex)
        var error=ReportError()
        error.message =ex.message
        error.status=HttpStatus.BAD_REQUEST.value()
        return ResponseEntity<ReportError>(error,HttpStatus.BAD_REQUEST)
    }*/

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
    fun handleInvalidFormatException(ex: InvalidFormatException, request: WebRequest): ResponseEntity<Any> {
        logger.error("400 Status Code", ex)
        val bodyOfResponse = GenericResponse(messageSource.getMessage("message.invalidJSON", null, request.locale), "InvalidJson")
        return handleExceptionInternal(ex, bodyOfResponse, HttpHeaders(), HttpStatus.BAD_REQUEST, request)
    }

    @ExceptionHandler(ScheduleUpdateException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleScheduleUpdateException(ex: ScheduleUpdateException, request: WebRequest): ResponseEntity<Any> {
        logger.error("400 Status Code", ex)
        val bodyOfResponse = GenericResponse(messageSource.getMessage("message.invalidScheduleupdateRequest", null, request.locale), ex.localizedMessage)
        return handleExceptionInternal(ex, bodyOfResponse, HttpHeaders(), HttpStatus.BAD_REQUEST, request)
    }


    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleInternal(ex: RuntimeException, request: WebRequest): ResponseEntity<Any> {
        logger.error("500 Status Code", ex)
        val bodyOfResponse = GenericResponse(messageSource.getMessage("message.error", null, request.locale), "InternalError")
        return ResponseEntity(bodyOfResponse, HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(CustomException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleCustomException(ex:RuntimeException,request: WebRequest):ResponseEntity<Any>{
        logger.error(ex)
        val bodyOfResponse=GenericResponse(messageSource.getMessage("message.error",null,request.locale),ex.message)
        return ResponseEntity(bodyOfResponse,HttpHeaders(),HttpStatus.BAD_REQUEST)
    }
    @ExceptionHandler(UndBusinessValidationException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    fun businessValidationError(ex: UndBusinessValidationException, request: WebRequest): ResponseEntity<Any> {
        logger.error("400 Status Code", ex)
        val bodyOfResponse = GenericResponse(messageSource.getMessage("message.error", null, request.locale), ex.error.toString())
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
        return ResponseEntity(bodyOfResponse, HttpHeaders(), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(EventUserListNotFoundException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun eventUser(ex: EventUserListNotFoundException, request: WebRequest): ResponseEntity<Any> {
        logger.error("401 Status Code", ex)
        val bodyOfResponse = GenericResponse(messageSource.getMessage("message.eventUserListNotFound", null, request.locale), ex.localizedMessage)
        return ResponseEntity(bodyOfResponse, HttpHeaders(), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(EventUserListBySegmentNotFoundException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun eventUser(ex: EventUserListBySegmentNotFoundException, request: WebRequest): ResponseEntity<Any> {
        logger.error("401 Status Code", ex)
        val bodyOfResponse = GenericResponse(messageSource.getMessage("message.segmentUserListNotFound", null, request.locale), ex.localizedMessage)
        return ResponseEntity(bodyOfResponse, HttpHeaders(), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(EventNotFoundException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun eventUser(ex: EventNotFoundException, request: WebRequest): ResponseEntity<Any> {
        logger.error("401 Status Code", ex)
        val bodyOfResponse = GenericResponse(messageSource.getMessage("message.eventNotFound", null, request.locale), ex.localizedMessage)
        return ResponseEntity(bodyOfResponse, HttpHeaders(), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(EventsListNotFoundException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun eventUser(ex: EventsListNotFoundException, request: WebRequest): ResponseEntity<Any> {
        logger.error("401 Status Code", ex)
        val bodyOfResponse = GenericResponse(messageSource.getMessage("message.eventListNotFound", null, request.locale), ex.localizedMessage)
        return ResponseEntity(bodyOfResponse, HttpHeaders(), HttpStatus.BAD_REQUEST)
    }



    @ExceptionHandler(EmailTemplateDuplicateNameException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun eventUser(ex: EmailTemplateDuplicateNameException, request: WebRequest): ResponseEntity<Any> {
        logger.error("400 Status Code", ex)
        val bodyOfResponse = GenericResponse(messageSource.getMessage("message.duplicateTemplateName", null, request.locale), ex.localizedMessage)
        return ResponseEntity(bodyOfResponse, HttpHeaders(), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(EmailTemplateNotFoundException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun eventUser(ex: EmailTemplateNotFoundException, request: WebRequest): ResponseEntity<Any> {
        logger.error("400 Status Code", ex)
        val bodyOfResponse = GenericResponse(messageSource.getMessage("message.emailTemplateNotFound", null, request.locale), ex.localizedMessage)
        return ResponseEntity(bodyOfResponse, HttpHeaders(), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(SegmentNotFoundException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun eventUser(ex: SegmentNotFoundException, request: WebRequest): ResponseEntity<Any> {
        logger.error("400 Status Code", ex)
        val bodyOfResponse = GenericResponse(messageSource.getMessage("message.nosegmentwithid", null, request.locale), ex.localizedMessage)
        return ResponseEntity(bodyOfResponse, HttpHeaders(), HttpStatus.BAD_REQUEST)
    }


    @ExceptionHandler(HttpClientErrorException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun clientError(ex: HttpClientErrorException, request: WebRequest): ResponseEntity<Any> {
        logger.error("400 Status Code", ex)
        val bodyOfResponse = GenericResponse(messageSource.getMessage("message.clientError", null, request.locale), ex.localizedMessage)
        return ResponseEntity(bodyOfResponse, HttpHeaders(), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(WrongCredentialException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleException(ex:WrongCredentialException,request: WebRequest):ResponseEntity<Any>{
        logger.error("400 Status Code", ex)
        val bodyOfResponse = GenericResponse(messageSource.getMessage("message.wrongCredential", null, request.locale), ex.localizedMessage)
        return ResponseEntity(bodyOfResponse, HttpHeaders(), HttpStatus.BAD_REQUEST)
    }


}
