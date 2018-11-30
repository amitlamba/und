package com.und.service

import com.und.common.utils.loggerFor
import com.und.model.jpa.FcmFailureAuditLog
import com.und.repository.jpa.FcmFailureAuditLogRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.stream.annotation.StreamListener
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


    @StreamListener("fcmFailureEventReceive")
    fun handleFcmFailure(notificationError:NotificationError){
        logger.info("fcm message failure is handled for clientid ${notificationError.clientId}")
        saveFcmFailure(notificationError)
        notificationError.clientId?.let {
            //pause all running campaign
//            campaignService.pauseAllRunning(it)
            //sending mail
//            emailService.sendNotificationConnectionErrorEmail(notificationError)
        }


    }

    private fun saveFcmFailure(notificationError: NotificationError){
        var log=FcmFailureAuditLog()
        with(log){
            clientID=notificationError.clientId
            status=notificationError.status
            message=notificationError.message
            errorCode=notificationError.errorCode
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
}