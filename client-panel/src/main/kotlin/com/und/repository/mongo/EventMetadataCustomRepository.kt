package com.und.repository.mongo

import com.und.model.mongo.CommonMetadata
import com.und.model.mongo.EventMetadata

interface EventMetadataCustomRepository {
    fun findAll(clientId: Long):List<EventMetadata>
}