package com.und.exception

import com.und.service.NotificationError

class FcmFailureException:RuntimeException {

    var error: NotificationError = NotificationError()

    constructor(error: NotificationError) : super() {
        this.error = error
    }

    constructor(message: String) : super(message)
}