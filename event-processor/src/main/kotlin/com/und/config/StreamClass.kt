package com.und.config

import com.und.utils.Constants
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.Output
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.SubscribableChannel


interface StreamClass {

    @Input(Constants.EVENT_QUEUE)
    fun inEvent():SubscribableChannel

//    @Output(Constants.EVENT_INTER_STATE)
//    fun outEvent():MessageChannel

    //listning fom event inter state queue
    @Input(Constants.SAVE_EVENT)
    fun saveEvent():SubscribableChannel
    //listning fom event inter state queue
    @Input(Constants.BUILD_METADATA)
    fun buildMetadata():SubscribableChannel
    //listning fom event inter state queue
    @Input(Constants.PROCESS_SEGMENT)
    fun processSegment():SubscribableChannel

    //listing on inEventUser chanel queue
    @Input(Constants.SAVE_USER)
    fun saveEventUser():SubscribableChannel
    //listing on inEventUser chanel queue
    @Input(Constants.BUILD_USER_METADATA)
    fun buildEventUserMetadata():SubscribableChannel
    //listing on inEventUser chanel queue
    @Input(Constants.PROCESS_USER_SEGMENT)
    fun userSegmentProcess():SubscribableChannel

    @Output("outEventForLiveSegment")
    fun outEventForLiveProcessing():MessageChannel

}