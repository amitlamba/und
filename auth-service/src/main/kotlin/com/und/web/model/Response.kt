package com.und.web.model

data class Response (
        var status: ResponseStatus = ResponseStatus.EMPTY,
        var data: Data = Data(),
        var message: String? = null,
        var validationError: ValidationError? = null

)

data class Data(
    var value: Any? = null,
    var message: String? = null
)

enum class ResponseStatus {
    SUCCESS,FAIL,ERROR,EMPTY
}