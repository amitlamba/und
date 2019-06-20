package com.und.service.eventapi

import com.und.config.EventStream
import com.und.web.model.eventapi.Event
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service

@Service
class EventTrackService {


    @Autowired
    private lateinit var eventStream: EventStream

    fun toKafka(event: Event): Boolean = eventStream.outTrackEvent().send(MessageBuilder.withPayload(event).build())
}