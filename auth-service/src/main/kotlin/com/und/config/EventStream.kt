package com.und.config

import org.springframework.cloud.stream.annotation.Output
import org.springframework.messaging.MessageChannel


interface EventStream {

    @Output("clientEmailSend")
    fun clientEmailSend(): MessageChannel

}