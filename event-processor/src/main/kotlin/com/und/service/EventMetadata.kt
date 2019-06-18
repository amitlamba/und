package com.und.service

import com.und.model.MongoEvent
import com.und.utils.Constants
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.stereotype.Service

@Service
class EventMetadata {

    @StreamListener(Constants.BUILD_METADATA)
    fun buildMetadata(event:MongoEvent){

    }
}