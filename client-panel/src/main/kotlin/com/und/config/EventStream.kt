package com.und.config

import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.Output
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.SubscribableChannel


interface EventStream {

    @Output("scheduleJobSend")
    fun scheduleJobSend(): MessageChannel

    @Input("scheduleJobAckReceive")
    fun scheduleJobAck(): SubscribableChannel

    @Output("clientEmailSend")
    fun clientEmailSend(): MessageChannel



}