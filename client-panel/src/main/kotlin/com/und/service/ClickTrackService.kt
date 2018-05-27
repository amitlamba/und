package com.und.service

import com.und.common.utils.loggerFor
import com.und.model.mongo.eventapi.ClickTrackEvent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Service
import org.springframework.util.concurrent.ListenableFutureCallback

@Service
class ClickTrackService {

    @Autowired
    private lateinit var kafkaTemplateClickTrack: KafkaTemplate<String, ClickTrackEvent>

    //TODO: should be picked from properties
    private var topic: String = "ClickTrack"

    companion object {
        protected val logger = loggerFor(ClickTrackService::class.java)
    }

    fun toKafka(clickTrackEvent: ClickTrackEvent): ClickTrackEvent {

        val future = kafkaTemplateClickTrack.send(topic, clickTrackEvent.clientId.toString(), clickTrackEvent)
        future.addCallback(object : ListenableFutureCallback<SendResult<String, ClickTrackEvent>> {
            override fun onSuccess(result: SendResult<String, ClickTrackEvent>) {
                logger.debug("Sent message: " + result)
            }

            override fun onFailure(ex: Throwable) {
                logger.error("Failed to send message", ex.message)
            }
        })
        return clickTrackEvent
    }
}