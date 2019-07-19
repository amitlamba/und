package com.und.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.und.config.EventStream
import com.und.exception.FcmFailureException
import com.und.fcmpush.feign.FcmFeignClient
import com.und.fcmpush.service.FcmHelperService
import com.und.fcmpush.service.FcmSendService
import com.und.fcmpush.service.FcmService
import com.und.model.utils.FcmMessage
import com.und.repository.jpa.security.UserRepository
import com.und.utils.loggerFor
import feign.FeignException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service("testfcmservice")
class TestFcmSendService: FcmService {

    companion object {
        protected val logger = loggerFor(FcmSendService::class.java)
    }

    @Autowired
    private lateinit var fcmFeignClient: FcmFeignClient
    @Autowired
    private lateinit var service: FcmHelperService
    @Autowired
    private lateinit var objectMapper: ObjectMapper
    @Autowired
    private lateinit var eventStream: EventStream
    @Autowired
    private lateinit var eventApiFeignClient: EventApiFeignClient
    @Autowired
    private lateinit var userRepository: UserRepository

    override fun sendMessage(message: FcmMessage) {
        var fcmMessageToSend = buildFcmMessage(message)
        var credential = service.getCredentials(message.clientId,message.serviceProviderId,message.type)
        if (credential == null) {
            logger.info("Credential does not exists for clientId ${message.clientId}")
//            var notificationError = NotificationError()
//            with(notificationError) {
//                clientId = message.clientId
//                this.message = "Service provider not found.First add a service provider"
//                errorCode = 400
//                campaignType=message.type
//                userId=message.userId
//                serviceProvider=null
//                serviceProviderId=null
//            }
//            toFcmFailureKafka(notificationError)
        } else {
//            var mongoFcmId = ObjectId().toString()
//            fcmMessageToSend.data.put("mongo_id", mongoFcmId)
//            fcmMessageToSend.data.put("campaign_id", message.campaignId.toString())
//            fcmMessageToSend.data.put("client_id", message.clientId.toString())
//            service.saveInMongo(message, FcmMessageStatus.NOT_SENT, mongoFcmId, credential.serviceProvider)
            var credentialMap = parseStringToMap(credential.credentialsMap)
            var serverKey = credentialMap.get("apiKey")!!

            var statusCode: Int? = 404
            try {
                statusCode = sendMessageToFcm(fcmMessageToSend, serverKey)
                if (statusCode == 200) {
                    logger.info("Fcm Send message successfuly for token= ${message.to}")

                } else {
                    throw FcmFailureException("Sending to fcm fail with status $statusCode")
                }
            } catch (ex: FeignException) {
                logger.info("Feign exception in sending fcm message ${ex}")
//                var notificationError = NotificationError()
//                with(notificationError) {
//                    if (ex.status() == 401) this.message = "UnAuthorized Please check your api key or update your android service provider"
//                    else this.message = ex.message
//                    to = message.to
//                    clientId = message.clientId
//                    status = ex.toString()
//                    errorCode = ex.status().toLong()
//                    campaignType=message.type
//                    userId=message.userId
//                    serviceProviderId=credential.id
//                    serviceProvider=credential.name
//                }
//                toFcmFailureKafka(notificationError)
            } catch (ex: FcmFailureException) {
                logger.info("Fcm Failure Exception with status code $statusCode message ${ex.message}")
//                var notificationError = NotificationError()
//                with(notificationError) {
//                    this.message = ex.message
//                    to = message.to
//                    clientId = message.clientId
//                    status = ex.toString()
//                    errorCode = statusCode?.toLong()
//                    campaignType=message.type
//                    userId=message.userId
//                    serviceProviderId=credential.id
//                    serviceProvider=credential.name
//                }
//                toFcmFailureKafka(notificationError)

            } catch (ex: Exception) {
                logger.info("Exception in sending fcm message $ex")
//                var notificationError = NotificationError()
//                with(notificationError) {
//                    this.message = ex.message
//                    to = message.to
//                    clientId = message.clientId
//                    status = ex.toString()
//                    errorCode = statusCode?.toLong()
//                    campaignType=message.type
//                    userId=message.userId
//                    serviceProviderId=credential.id
//                    serviceProvider=credential.name
//                }
//                toFcmFailureKafka(notificationError)
            }

        }
    }

    private fun buildFcmMessage(message: com.und.model.utils.FcmMessage): com.und.model.mongo.LegacyFcmMessage {
        when (message.type) {
            "android" -> return service.buildFcmAndroidMessage(message)
//            "ios"->service.buildIosMessage(message)
            "web" -> return service.buildWebFcmMessage(message)
            else -> return com.und.model.mongo.LegacyFcmMessage()
        }
    }

    private fun sendMessageToFcm(fcmMessage: com.und.model.mongo.LegacyFcmMessage, serverKey: String): Int {
        var auth = "key=$serverKey"
        var response = fcmFeignClient.pushMessage(auth, objectMapper.writeValueAsString(fcmMessage))
        if(response.statusCodeValue == 200) {
            var body: LinkedHashMap<String, Any> = response.body as LinkedHashMap<String, Any>
            var success = body["success"]
            if (success.toString().toInt() > 0)
                return 200
            else
                throw FcmFailureException("Fcm message fail with error ${body["results"]}")
        }
        return response.statusCodeValue
    }

    private fun parseStringToMap(jsonString: String): HashMap<String, String> {
        return objectMapper.readValue(jsonString)
    }

//    private fun toFcmFailureKafka(notificationError: NotificationError) {
//        eventStream.fcmFailureEventSend().send(MessageBuilder.withPayload(notificationError).build())
//    }
}