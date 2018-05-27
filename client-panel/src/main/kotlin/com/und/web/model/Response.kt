package com.und.web.model

data class Response<T> (
    var status: ResponseStatus = ResponseStatus.EMPTY,
    var data: Data<T> = Data(),
    var message: String? = null,
    var validationError: ValidationError? = null

)

data class Data<T>(
    var value: T? = null,
    var message: String? = null
)