//package com.und.repository.mongo
//
//import com.und.model.mongo.eventapi.CommonMetadata
//import com.und.model.mongo.eventapi.EventMetadata
//import org.springframework.data.mongodb.repository.MongoRepository
//import org.springframework.stereotype.Repository
//
//typealias TechnoGraphics = CommonMetadata
//typealias AppFields = CommonMetadata
//
//@Repository
//interface CommonMetadataRepositoryCustom {
//
//    fun save(commonMetadata: CommonMetadata,clientId: Long)
//    fun findByName(name: String,clientId: Long):CommonMetadata?
//    fun findAll(clientId: Long):List<CommonMetadata>
//
//    fun updateTechnographics(clientId: Long, technographics: TechnoGraphics): CommonMetadata?
//
//    fun updateAppFields(clientId: Long, appFields: AppFields): CommonMetadata?
//}
//
//
//
