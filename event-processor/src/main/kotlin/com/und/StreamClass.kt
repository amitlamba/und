package com.und

import com.und.utils.Constants
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.Output
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.SubscribableChannel


interface StreamClass {

    @Input(Constants.EVENT_QUEUE)
    fun inEvent():SubscribableChannel

    @Output(Constants.EVENT_INTER_STATE)
    fun outEvent():MessageChannel

    @Input(Constants.SAVE_EVENT)
    fun saveEvent():SubscribableChannel

    @Input(Constants.BUILD_METADATA)
    fun buildMetadata():SubscribableChannel

    @Input(Constants.PROCESS_SEGMENT)
    fun processSegment():SubscribableChannel
}