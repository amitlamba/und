package com.und.config

import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.Output
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.SubscribableChannel


interface EventStream {

    @Output("scheduleJobSend")
    fun scheduleJobSend(): MessageChannel

    @Output("scheduleLiveJobSend")
    fun scheduleLiveJobSend(): MessageChannel

    @Input("scheduleJobAckReceive")
    fun scheduleJobAck(): SubscribableChannel

    @Input("emailFailureEventReceive")
    fun emailFailureEventReceive(): SubscribableChannel

    @Input("fcmFailureEventReceive")
    fun fcmFailureEventReceive():SubscribableChannel

    @Output("clientEmailSend")
    fun clientEmailSend(): MessageChannel

    @Input("inEventForLiveSegment")
    fun inEventForLiveSegment(): SubscribableChannel

    @Input("inJobForLiveSegmentCheck")
    fun inJobForLiveSegment(): SubscribableChannel

    @Output("outLiveSegment")
    fun outLiveSegment(): MessageChannel

    @Output("outTestCampaign")
    fun outTestCampaign():MessageChannel

    @Output("triggerManualCampaign")
    fun triggerManualCampaign():MessageChannel

    @Output("outSegment")
    fun outSegment():MessageChannel

    @Input("inSegment")
    fun inSegment():SubscribableChannel

    @Output("outSegmentScheduleJob")
    fun outSegmentScheduleJob():MessageChannel

    @Input("inComputeSegment")
    fun inComputeSegment():SubscribableChannel
}