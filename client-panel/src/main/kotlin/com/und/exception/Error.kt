package com.und.exception

class EmailError {

    var clientid: Long? = null
    var failureType = FailureType.NONE
    var causeMessage: String? = null
    var errorCode:String?=null
    var invalidAddresses: List<String> = emptyList()
    var validSentAddresses: List<String> = emptyList()
    var unsentAddresses: List<String> = emptyList()
    var errorType:String?=null
    var failedSettingId: Long? = null
    enum class FailureType {
        CONNECTION, DELIVERY, INCORRECT_EMAIL, OTHER, NONE
    }

}