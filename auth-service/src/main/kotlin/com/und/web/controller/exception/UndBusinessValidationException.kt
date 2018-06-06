package com.und.web.controller.exception

import com.und.web.controller.errorhandler.ValidationError

class UndBusinessValidationException (
        var error: ValidationError
) : Exception()
