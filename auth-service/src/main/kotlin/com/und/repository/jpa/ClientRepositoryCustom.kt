package com.und.repository.jpa

interface ClientRepositoryCustom {
    fun userpropertiesExists(clientId:Long):Boolean
    fun eventMetadtaExists(clientId:Long):Boolean
    fun saveUserProperties(clientId: Long, metadataJson: String)
    fun saveEventMetadta(clientId: Long, metadataJson: String)
}