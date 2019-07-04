package com.und.config

import com.und.utils.Constants
import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.Output
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.SubscribableChannel

interface StreamClass {

    @Input(Constants.SAVE_EVENT)
    fun saveEvent():SubscribableChannel
    @Input(Constants.BUILD_METADATA)
    fun buildMetadata():SubscribableChannel
    @Input(Constants.PROCESS_SEGMENT)
    fun processSegment():SubscribableChannel
    @Input(Constants.SAVE_USER)
    fun saveEventUser():SubscribableChannel
    @Input(Constants.BUILD_USER_METADATA)
    fun buildEventUserMetadata():SubscribableChannel
    @Input(Constants.PROCESS_USER_SEGMENT)
    fun userSegmentProcess():SubscribableChannel
    @Output(Constants.OUT_EVENT_LIVE_SEGMENT)
    fun outEventForLiveProcessing():MessageChannel
    @Input(Constants.IN_EMAIL_READ)
    fun inEmailRead():SubscribableChannel
    @Input(Constants.IN_NOTIFICATION_READ)
    fun inNotificationRead():SubscribableChannel
    @Input(Constants.IN_TRACK_EVENT)
    fun inTrackEvent():SubscribableChannel
    @Output(Constants.OUT_EVENTUSER)
    fun outEventUser():MessageChannel
    @Input(Constants.IN_EVENTUSER)
    fun inEventUser():SubscribableChannel

}