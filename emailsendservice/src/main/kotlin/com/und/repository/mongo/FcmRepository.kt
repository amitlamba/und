package com.und.repository.mongo

import com.und.model.mongo.AnalyticFcmMessage
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface FcmRepository:MongoRepository<AnalyticFcmMessage,String> ,FcmCustomRepository
