package com.und.service


import com.und.model.FcmMessageStatus
import com.und.model.FcmMessageUpdates
import com.und.model.NotificationRead
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class FcmService {

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @StreamListener("inNotificationRead")
    fun listeningWebpushNotification(message: NotificationRead){
        var query= Query.query(Criteria("_id").`is`(ObjectId(message.mongoId)).and("status").ne(FcmMessageStatus.READ))
        var update= Update().push("statusUpdates", FcmMessageUpdates(LocalDateTime.now(), FcmMessageStatus.READ))
                .set("status", FcmMessageStatus.READ)
        mongoTemplate.updateFirst(query,update,resolveFcmCollectionName(message.clientId,message.type))

//        var mongoMessage=mongoTemplate.findOne(query,AnalyticFcmMessage::class.java,resolveFcmCollectionName(message.clientId,message.type))
//
//        //FIXME user agent not set lat,long,identity not set neither in email,sms.
//        val event = Event()
//        with(event) {
//            name = "Notification Click"
//            clientId = message.clientId
//            notificationId = message.mongoId
//            //TODO we can pass campaign id from sdk
//            attributes["campaignId"] = mongoMessage.campaignId
//            timeZone=AuthenticationUtils.principal.timeZoneId
//        }
//        eventService.toKafka(event)
    }

    private fun resolveFcmCollectionName(clientId: Long,type: String):String{
        when(type){
            "android" -> return "${clientId}_fcmMessage"
            "web" -> return "${clientId}_webFcmMessage"
            "ios" -> return "${clientId}_iosFcmMessage"
            else -> return ""
        }
    }
}


