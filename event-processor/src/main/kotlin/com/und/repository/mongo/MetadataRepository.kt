package com.und.repository.mongo

import com.und.model.mongo.Metadata
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface MetadataRepository : MongoRepository<Metadata,Long> {

    fun findByClientIdAndTypeAndStopped(clientId:Long,status:String,stopped:Boolean):List<Metadata>
    fun findByClientId(clientId: Long):List<Metadata>
}