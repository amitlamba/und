package com.und.exception

import javax.mail.internet.InternetAddress

class EmailFailureException : RuntimeException {

    var error: EmailError = EmailError()

    constructor(error: EmailError) : super() {
        this.error = error
    }

    constructor(message: String) : super() {
        this.error.causeMessage = message
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
    var errorCode: String? = null
    var invalidAddresses: List<String> = emptyList()
    var validSentAddresses: List<String> = emptyList()
    var unsentAddresses: List<String> = emptyList()
    var from: String? = null
    var failedSettingId: Long? = null
    var errorType: String? = null
    var retry: Boolean = false
    var retries: Int = 0

    enum class FailureType {
        CONNECTION, DELIVERY, INCORRECT_EMAIL, OTHER, NONE
    }

}

enum class Connection {
    MessageRejected,   //from addresss not verified
    InvalidParameterValue,  //if domain part missing
    InvalidClientTokenId,  //access id wrong
    SignatureDoesNotMatch, // access seceret id wrong
    AccessDenied,       //not enough permisssion to iam user
    ConfigurationSetDoesNotExist,  //config set not exist
}
