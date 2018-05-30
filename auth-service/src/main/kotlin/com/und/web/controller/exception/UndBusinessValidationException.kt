package com.und.web.controller.exception

import com.und.web.model.ValidationError

class UndBusinessValidationException (
        var error: ValidationError
) : Exception()
