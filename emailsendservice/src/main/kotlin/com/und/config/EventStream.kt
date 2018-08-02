package com.und.config

import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.Output
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.SubscribableChannel


interface EventStream {

    @Output("emailEventSend")
    fun emailEventSend(): MessageChannel

    @Output(value="smsEventSend")
    fun smsEventSend():MessageChannel

    @Output(value="VerificationEmailReceive")
    fun verificationEmailReceive():SubscribableChannel

    @Input("clientEmailReceive")
    fun clientEmailSend(): SubscribableChannel

    @Input("EmailUpdateReceive")
    fun emailUpdateEvent(): SubscribableChannel

    @Input("clickTrackEventReceive")
    fun clickTrackEvent(): SubscribableChannel

    @Input("campaignTriggerReceive")
    fun campaignTriggerReceive(): SubscribableChannel

}