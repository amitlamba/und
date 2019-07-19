package com.und.campaign.repository.mongo

import com.und.model.mongo.EventUser
import com.und.model.utils.CampaignType
import org.bson.types.ObjectId
import org.springframework.stereotype.Repository

@Repository
interface CustomEventUserRepository {

    fun findByEmail(clientId:Long,email:String):List<EventUser>
    fun findByUndId(clientId: Long,undId:String):List<EventUser>
    fun findByUid(clientId: Long,uid:String):List<EventUser>
    fun findByMobile(clientId: Long,mobile: Long):List<EventUser>
    fun findByAndroidFcmToken(clientId: Long,token:String):List<EventUser>
    fun findByWebFcmToken(clientId: Long,token: String):List<EventUser>

    fun findByEmailIn(clientId: Long,email:Array<String>):List<EventUser>
    fun findByUndIdIn(clientId: Long,undid:Array<String>):List<EventUser>
    fun findByUidIn(clientId: Long,uid:Array<String>):List<EventUser>
    fun findByMobileIn(clientId: Long,mobile:Array<String>):List<EventUser>
    fun findByAndroidFcmTokenIn(clientId: Long,token:Array<String>):List<EventUser>
    fun findByWebFcmTokenIn(clientId: Long,token:Array<String>):List<EventUser>

    fun findAllById(clientId: Long,ids:List<ObjectId>):List<EventUser>

    fun findAllByIdAndByCampaignType(clientId: Long, ids: List<ObjectId>, type:CampaignType):List<EventUser>

}