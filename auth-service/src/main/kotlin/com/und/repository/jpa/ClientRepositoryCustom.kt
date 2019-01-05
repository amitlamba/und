package com.und.repository.jpa

interface ClientRepositoryCustom {
    fun saveUserProperties(clientId: Long, metadataJson: String)
    fun saveEventMetadta(clientId: Long, metadataJson: String)
}