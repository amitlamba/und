package com.und.exception

class EmailFailureException : RuntimeException {

    var error: EmailError = EmailError()

    constructor(error: EmailError) : super() {
        this.error = error
    }

    constructor(message: String, cause: Throwable, error: EmailError) : super(message, cause) {
        this.error = error
    }

    constructor(message: String, error: EmailError) : super(message) {
        this.error = error
    }

    constructor(cause: Throwable, error: EmailError) : super(cause) {
        this.error = error
    }


}

class EmailError {

    var clientid: Long? = null
    var failureType = FailureType.NONE
    var causeMessage: String? = null
    var invalidAddresses: List<String> = emptyList()
    var validSentAddresses: List<String> = emptyList()
    var validUnsentAddresses: List<String> = emptyList()
    var from: String? = null
    var failedSettingId: Long? = null

    enum class FailureType {
        CONNECTION, DELIVERY, INCORRECT_EMAIL, OTHER, NONE
    }

}

