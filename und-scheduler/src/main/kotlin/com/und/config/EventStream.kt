package com.und.config

import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.Output
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.SubscribableChannel

interface EventStream {

    @Input("scheduleJobReceive")
    fun scheduleJobReceive(): SubscribableChannel

    @Output("scheduleJobAckSend")
    fun scheduleJobAck(): MessageChannel

    @Output("campaignTriggerSend")
    fun campaignTriggerEvent(): MessageChannel



}