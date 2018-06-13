package com.und.web.controller.exception

import com.und.web.controller.errorhandler.ValidationError

class EmailAlreadyRegisteredException (
        var error: ValidationError
) : Exception()
