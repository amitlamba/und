package com.und.config

import org.springframework.cloud.stream.annotation.Input
import org.springframework.cloud.stream.annotation.Output
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.SubscribableChannel


interface EventStream {

    @Input("inEvent")
    fun inEvent(): SubscribableChannel

    @Output("outEvent")
    fun outEvent(): MessageChannel

    @Input("inEventUser")
    fun inEventUser(): SubscribableChannel

    @Output("outEventUser")
    fun outEventUser(): MessageChannel

    @Input("inProcessEventUserProfile")
    fun inProcessEventUserProfile(): SubscribableChannel

    @Output("outProcessEventUserProfile")
    fun outProcessEventUserProfile(): MessageChannel

    @Input("inTrackEvent")
    fun inTrackEvent(): SubscribableChannel

    @Output("outTrackEvent")
    fun outTrackEvent(): MessageChannel

    @Input("inEmailRead")
    fun inEmailRead(): SubscribableChannel

    @Output("outEmailRead")
    fun outEmailRead(): MessageChannel

    @Output("outEventForLiveSegment")
    fun outEventForLiveSegment(): MessageChannel

    @Output("outNotificationRead")
    fun outNotificationRead():MessageChannel

    @Input("inNotificationRead")
    fun inNotificationRead():SubscribableChannel
/*    @Output("event")
    fun readEvent(): SubscribableChannel

    @Output("eventUser")
    fun readEventUser(): SubscribableChannel

    @Output("processedEventUserProfile")
    fun readEventUserProfile(): SubscribableChannel*/



}