package com.und.config

import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.Output
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.SubscribableChannel


interface EventStream {

    @Output("emailEventSend")
    fun emailEventSend(): MessageChannel

    @Input("emailEventReceive")
    fun emailEventReceive(): SubscribableChannel

    @Output(value="smsEventSend")
    fun smsEventSend():MessageChannel

    @Input(value="smsEventReceive")
    fun smsEventReceive():SubscribableChannel

    @Output(value="emailFailureEventSend")
    fun emailFailureEventSend():MessageChannel

    @Output("fcmEventSend")
    fun fcmEventSend():MessageChannel

    @Input(value="fcmEventReceive")
    fun fcmEventReceive():SubscribableChannel

    @Output("fcmFailureEventSend")
    fun fcmFailureEventSend():MessageChannel

    @Input(value="VerificationEmailReceive")
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