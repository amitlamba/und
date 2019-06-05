package com.und.repository.mongo

import com.und.model.mongo.CommonMetadata

interface CommonMetadataCustomRepository {
    fun findAll(clientId: Long):List<CommonMetadata>
}