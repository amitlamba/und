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

    @Input("emailFailureEventReceive")
    fun emailFailureEventReceive(): SubscribableChannel

    @Input("fcmFailureEventReceive")
    fun fcmFailureEventReceive():SubscribableChannel

    @Output("clientEmailSend")
    fun clientEmailSend(): MessageChannel

    @Output(value = "VerificationEmailSend")
    fun verificationEmailReceive(): MessageChannel

    @Input("inEventForLiveSegment")
    fun inEventForLiveSegment(): SubscribableChannel

    @Input("inJobForLiveSegmentCheck")
    fun inJobForLiveSegment(): SubscribableChannel

    @Output("outLiveSegment")
    fun outLiveSegment(): MessageChannel
}