package com.und.repository.mongo

import com.und.model.mongo.eventapi.CommonMetadata
import com.und.model.mongo.eventapi.EventMetadata
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

typealias TechnoGraphics = CommonMetadata
typealias AppFields = CommonMetadata

@Repository
interface CommonMetadataRepositoryCustom {

    fun updateTechnographics(clientId: Long, name: TechnoGraphics): CommonMetadata?

    fun updateAppFields(clientId: Long, name: AppFields): CommonMetadata?
}



