package com.und.repository.mongo

import com.und.model.mongo.CommonMetadata
import com.und.model.mongo.EventMetadata
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface EventMetadataRepository:MongoRepository<EventMetadata,String>,EventMetadataCustomRepository {
}





