package com.und.service.eventapi

import com.und.model.mongo.eventapi.CommonMetadata
import com.und.model.mongo.eventapi.EventMetadata
import com.und.repository.mongo.CommonMetadataRepository
import com.und.repository.mongo.EventMetadataRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class EventMetadataService {

    @Autowired
    lateinit var eventMetadataRepository: EventMetadataRepository

    @Autowired
    lateinit var commonMetadataRepository: CommonMetadataRepository

    fun getEventMetadata(clientId:Long): List<EventMetadata> {
        return eventMetadataRepository.findAll(clientId)
    }

    fun getCommonProperties(clientId:Long): List<CommonMetadata> {
        return commonMetadataRepository.findAll(clientId)
    }
}