package com.und.repository.mongo

import com.und.model.mongo.eventapi.CommonMetadata
import com.und.model.mongo.eventapi.EventMetadata
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository



@Repository
interface CommonMetadataRepository :CommonMetadataRepositoryCustom, MongoRepository<CommonMetadata, String> {
    fun findByName(name: String):CommonMetadata?

}



