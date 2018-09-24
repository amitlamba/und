package com.und.exception

class EmailError {

    var clientid: Long? = null
    var failureType = FailureType.NONE
    var causeMessage: String? = null
    var invalidAddresses: List<String> = emptyList()
    var validSentAddresses: List<String> = emptyList()
    var validUnsentAddresses: List<String> = emptyList()
    var failedSettingId: Long? = null

    enum class FailureType {
        CONNECTION, DELIVERY, INCORRECT_EMAIL, OTHER, NONE
    }

}