package com.und.repository.jpa

import com.und.model.mongo.CommonMetadata

interface ClientRepositoryCustom {
    fun userpropertiesExists(clientId:Long):Boolean
    fun eventMetadtaExists(clientId:Long):Boolean
    fun saveUserProperties(clientId: Long, metadataJson: List<CommonMetadata>)
    fun saveEventMetadta(clientId: Long, metadataJson: String)
    fun getCommonUserProperties():List<CommonMetadata>
}