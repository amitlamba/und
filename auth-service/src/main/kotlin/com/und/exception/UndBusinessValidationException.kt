package com.und.exception

import com.und.web.model.ValidationError

class UndBusinessValidationException (
        var error: ValidationError
) : Exception()
