package com.und.service

import com.und.common.utils.loggerFor
import com.und.model.jpa.FcmFailureAuditLog
import com.und.repository.jpa.FcmFailureAuditLogRepository
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Service

@Service
class FcmFailureHandler {

    companion object {
        protected val logger = loggerFor(FcmFailureHandler::class.java)
    }

    @Autowired
    private lateinit var fcmFailureAuditLogRepository: FcmFailureAuditLogRepository

    @Autowired
    private lateinit var campaignService: CampaignService

    @Autowired
    private lateinit var emailService: EmailService

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @StreamListener("fcmFailureEventReceive")
    fun handleFcmFailure(notificationError:NotificationError){
        logger.info("fcm message failure is handled for clientid ${notificationError.clientId}")
        saveFcmFailure(notificationError)
        notificationError.clientId?.let {
            //pause all running campaign
//            campaignService.pauseAllRunning(it)
            //sending mail
            emailService.sendNotificationConnectionErrorEmail(notificationError)

            //if error is NotRegistered & InvalidRegistration then remove token from eventuser in mongo
            if(notificationError.campaignType.equals("android")){
                if(notificationError.message!!.contains(regex = Regex(".*(NotRegistered|InvalidRegistration).*"))){
                    mongoTemplate.updateFirst(Query.query(Criteria("_id").`is`(ObjectId(notificationError.userId))), Update.update("androidFcmToken",null).set("communication.android",null),"${notificationError.clientId}_eventUser")
                }
            }
            if(notificationError.campaignType.equals("web")){
                if(notificationError.message!!.contains(regex = Regex(".*(NotRegistered|InvalidRegistration).*"))){
                    mongoTemplate.updateFirst(Query.query(Criteria("_id").`is`(ObjectId(notificationError.userId))), Update().pull("identity.webFcmToken",notificationError.to),"${notificationError.clientId}_eventUser")
                }
            }
            if(notificationError.campaignType.equals("ios")){
                if(notificationError.message!!.contains(regex = Regex(".*(NotRegistered|InvalidRegistration).*"))){
                    mongoTemplate.updateFirst(Query.query(Criteria("_id").`is`(ObjectId(notificationError.userId))), Update.update("iosFcmToken",null).set("communication.ios",null),"${notificationError.clientId}_eventUser")
                }
            }
        }


    }

    private fun saveFcmFailure(notificationError: NotificationError){
        var log=FcmFailureAuditLog()
        with(log){
            clientID=notificationError.clientId
            status=notificationError.status
            message=notificationError.message
            errorCode=notificationError.errorCode
            type=notificationError.campaignType
        }
        fcmFailureAuditLogRepository.save(log)
    }
}

class NotificationError{
    var to:String?=null
    var status:String?=null
    var message:String?=null
    var clientId: Long?=null
    var errorCode:Long?=null
    var campaignType:String?=null
    var userId:String?=null
}