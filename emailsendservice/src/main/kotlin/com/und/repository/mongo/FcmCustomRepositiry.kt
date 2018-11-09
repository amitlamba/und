package com.und.repository.mongo

import com.und.model.mongo.AnalyticFcmMessage
import com.und.model.mongo.FcmMessageStatus
import org.springframework.stereotype.Repository

@Repository
interface FcmCustomRepository {
    fun saveAnalyticMessage(message: AnalyticFcmMessage,clientId:Long)
    fun updateStatus(mongoId:String,status: FcmMessageStatus,clientId: Long,clickTrackEventId: String?)
}