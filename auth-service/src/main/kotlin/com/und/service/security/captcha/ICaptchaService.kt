package com.und.service.security.captcha

import com.und.web.controller.exception.ReCaptchaInvalidException


interface ICaptchaService {

    val reCaptchaSite: String?

    val reCaptchaSecret: String?
    
    @Throws(ReCaptchaInvalidException::class)
    fun processResponse(response: String)
}
