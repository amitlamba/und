package com.und.fcmpush.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.und.campaign.repository.jpa.AndroidCampaignRepository
import com.und.campaign.repository.jpa.CampaignRepository
import com.und.campaign.repository.jpa.EmailCampaignRepository
import com.und.campaign.repository.jpa.WebPushCampaignRepository
import com.und.campaign.repository.mongo.EventUserRepository
import com.und.common.utils.BuildCampaignMessage
import com.und.config.EventStream
import com.und.exception.FcmFailureException
import com.und.model.mongo.FcmMessageStatus
import com.und.model.utils.eventapi.Event
import com.und.model.utils.eventapi.Identity
import com.und.repository.jpa.security.UserRepository
import com.und.service.EventApiFeignClient
import com.und.fcmpush.feign.FcmFeignClient
import com.und.model.utils.CampaignType
import com.und.model.utils.LiveCampaignTriggerInfo
import com.und.utils.loggerFor
import feign.FeignException
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.messaging.support.MessageBuilder
import java.io.*
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class FcmSendService: FcmService {

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
    private lateinit var userRepository:UserRepository

    @Autowired
    private lateinit var buildCampaignMessage: BuildCampaignMessage

    @Autowired
    private lateinit var campaignRepository: CampaignRepository

    @Autowired
    private lateinit var emailCampaignRepository: EmailCampaignRepository

    @Autowired
    private lateinit var eventUserRepository: EventUserRepository

    @Autowired
    private lateinit var androidCampaignRepository: AndroidCampaignRepository

    @Autowired
    private lateinit var webPushCampaignRepository: WebPushCampaignRepository

    fun sendMessage(clientId: Long, authKey: String, message: FcmMessage): ResponseEntity<Any?>? {
        try {
            return fcmFeignClient.pushMessage("key=" + authKey, jacksonObjectMapper().writeValueAsString(message))
        } catch (ex: FeignException) {
           // println("Feign Error Code: ${ex.status()}, Message: ${ex.message}")
            ex.printStackTrace()
        }
        return null
    }

//    fun sendMessage(clientId: Long, authKey: String, notificationTitle: String, notificationText: String,
//                    to: String): ResponseEntity<Any?>? {
//        val message = FcmMessage(to = to, notification = NotificationPayloadWeb(title = notificationTitle, body = notificationText))
//        return sendMessage(clientId, authKey, message)
//    }


//    Send Message to a user
//
//    POST https://fcm.googleapis.com/v1/projects/myproject-b5ae1/messages:send HTTP/1.1
//
//    Content-Type: application/json
//    Authorization: Bearer ya29.ElqKBGN2Ri_Uz...HnS_uNreA
//
//    {
//      "message":{
//        "token" : "bk3RNwTe3H0:CI2k_HHwgIpoDKCIZvvDMExUdFQ3P1...",
//        "notification" : {
//          "body" : "This is an FCM notification message!",
//          "title" : "FCM Message",
//          }
//       }
//    }

    fun sendMessage(message: FcmMessage) {
        try {
            var reader = BufferedReader(FileReader(File("/home/jogendra/Desktop/cloudmessaging.json")))
            var file = reader.readLine()
            var sb = StringBuilder()
            while (file != null) {
                sb.append(file)
                file = reader.readLine()
            }
            //println(sb.toString())
            var accessToken = getAccessToken(sb.toString())
            //println("access token $accessToken")
            //println(send(message, accessToken))
//                var res=fcmFeignClient.pushMessage(message.project_id,"Bearer"+accessToken, jacksonObjectMapper().writeValueAsString(message))
//                return res
        } catch (ex: Exception) {
//            return ResponseEntity(HttpStatus.UNPROCESSABLE_ENTITY)
        }
//        return ResponseEntity(HttpStatus.OK)
    }

    private fun getAccessToken(file: String): String {
        var credential = GoogleCredential.fromStream(ByteArrayInputStream(file.toByteArray())).createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
        credential.refreshToken()
        return credential.accessToken
    }

    fun send(message: FcmMessage, token: String): Int {
        var url = URL("https://fcm.googleapis.com/v1/projects/${message.project_id}/messages:send")
        var conn = url.openConnection() as HttpsURLConnection
        conn.setRequestProperty("Authorization", "Bearer ${token}")
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        var outputstream = conn.outputStream
        var mapper = ObjectMapper()
        var out = mapper.writeValueAsString(message.message)
//        print("message is $out")
        outputstream.write(out.toByteArray())
        outputstream.flush()
        outputstream.close()
//        print(conn.responseMessage)
        return conn.responseCode

    }

    class FcmMessage {
        lateinit var message: TestMessage1
        lateinit var project_id: String
    }

    class TestMessage1 {
        lateinit var message: TestMessage
    }
//    class FcmMessage{
//        //only one of 1st 3 property
//        var token:String?=null
//        var topic:String?=null
//        var condition:String?=null
//        lateinit var project_id:String
//        var name:String?=null
//        var notification:CommonNotification?=null
//        var data:HashMap<String,String>?=null
//        var android:AndroidConfig?=null
//    //var webpush:WebConfig?=null
//    //var apns:ApnsConfig?=null
//    }

    class CommonNotification {
        var title: String? = null
        var body: String? = null
    }

    class AndroidConfig {
        var collapse_key: String? = null
        var ttl: String? = null
        var data: HashMap<String, String>? = null
        var notification: AndroidNotification? = null
        var priority: String? = null  //enum High Normal
    }

    class AndroidNotification {
        var title: String? = null
        var body: String? = null
        var sound: String? = null //  /res/raw/1.mp3 //optional
        var color: String? = null //  #ffffff   //optional
        var icon: String? = null  //  drawable/pic1 //optional
        var tag: String? = null   //optional
        var click_action: String? = null

        var body_loc_key: String? = null
        var body_loc_args: List<String>? = null

    }

    /*


    Send Message to Multiple Devices

    POST https://fcm.googleapis.com/v1/projects/myproject-b5ae1/messages:send HTTP/1.1

    Content-Type: application/json
    Authorization: Bearer ya29.ElqKBGN2Ri_Uz...HnS_uNreA
    {
      "message":{
        "topic" : "foo-bar",
        "notification" : {
          "body" : "This is a Firebase Cloud Messaging Topic Message!",
          "title" : "FCM Message",
          }
       }
    }
     */

    /*
    Send to Single Topic

    https://fcm.googleapis.com/fcm/send
    Content-Type:application/json
    Authorization:key=AIzaSyZ-1u...0GBYzPu7Udno5aA
    {
      "to" : "/topics/foo-bar",
      "priority" : "high",
      "notification" : {
        "body" : "This is a Firebase Cloud Messaging Topic Message!",
        "title" : "FCM Message",
      }
    }
     */

    /*
    Send to Device Group

    https://fcm.googleapis.com/fcm/send
    Content-Type:application/json
    Authorization:key=AIzaSyZ-1u...0GBYzPu7Udno5aA

    {
      "to": "aUniqueKey",
      "data": {
        "hello": "This is a Firebase Cloud Messaging Device Group Message!",
       }
    }
     */

     fun sendLiveMessage(infoModel: LiveCampaignTriggerInfo) {
        val campaign = campaignRepository.findById(infoModel.campaignId).get()
        val user = eventUserRepository.findByIdAndClientId(ObjectId(infoModel.userId),infoModel.clientId)
        user?.let {
            when(campaign.campaignType){
                CampaignType.PUSH_WEB.name -> {
                    val androidCampaign = androidCampaignRepository.findByCampaignId(infoModel.campaignId).get()
                    val message = buildCampaignMessage.buildAndroidFcmMessage(infoModel.clientId,androidCampaign,user,campaign,infoModel.templateId)
                    sendMessage(message)
                }
                CampaignType.PUSH_WEB.name -> {
                    val webPushCampaign = webPushCampaignRepository.findByCampaignId(infoModel.campaignId).get()
                            user.identity.webFcmToken?.forEach {
                                val message = buildCampaignMessage.buildWebFcmMessage(infoModel.clientId,webPushCampaign,it,campaign,user,infoModel.templateId)
                                sendMessage(message)
                            }
                }
                else -> {
                    logger.info("wrong campaign type (${campaign.campaignType}) expected only web and android.")
                }
            }

        }
    }
    override fun sendMessage(message: com.und.model.utils.FcmMessage) {
        var fcmMessageToSend = buildFcmMessage(message)
        var credential = service.getCredentials(message.clientId,message.serviceProviderId,message.type)
        if (credential == null) {
            logger.info("Credential does not exists for clientId ${message.clientId}")
            var notificationError = NotificationError()
            with(notificationError) {
                clientId = message.clientId
                this.message = "Service provider not found.First add a service provider"
                errorCode = 400
                campaignType=message.type
                userId=message.userId
                serviceProvider=null
                serviceProviderId=null
            }
            throw FcmFailureException(notificationError)
            //toFcmFailureKafka(notificationError)
        } else {
            var mongoFcmId = ObjectId().toString()
            fcmMessageToSend.data.put("mongo_id", mongoFcmId)
            fcmMessageToSend.data.put("campaign_id", message.campaignId.toString())
            fcmMessageToSend.data.put("template_id", message.templateId.toString())
            fcmMessageToSend.data.put("client_id", message.clientId.toString())
            service.saveInMongo(message, FcmMessageStatus.NOT_SENT, mongoFcmId, credential.serviceProvider)
            var credentialMap = parseStringToMap(credential.credentialsMap)
            var serverKey = credentialMap.get("apiKey")!!

            var statusCode: Int? = 404
            try {
                statusCode = sendMessageToFcm(fcmMessageToSend, serverKey)
                if (statusCode == 200) {
                    service.updateStatus(mongoFcmId, FcmMessageStatus.SENT, message.clientId,message.type)
                    logger.info("Fcm Send message successfuly for token= ${message.to}")

                    val token = userRepository.findSystemUser().key
                    var event= Event()
                    with(event) {
                        name = "Notification Sent"
                        clientId=message.clientId
                        notificationId=mongoFcmId
                        attributes.put("campaign_id",message.campaignId)
                        attributes.put("template_id",message.templateId)
                        userIdentified=true
                        identity= Identity(userId = message.userId,clientId = message.clientId.toInt(),idf=1)

                    }
                    eventApiFeignClient.pushEvent(token,event)

                } else {
                    throw FcmFailureException("Sending to fcm fail with status $statusCode")
                }
            } catch (ex: FeignException) {
                service.updateStatus(mongoFcmId, FcmMessageStatus.ERROR, message.clientId,message.type)
                logger.info("Feign exception in sending fcm message ${ex}")
                var notificationError = NotificationError()
                with(notificationError) {
                    if (ex.status() == 401) this.message = "UnAuthorized Please check your api key or update your android service provider"
                    else this.message = ex.message
                    to = message.to
                    clientId = message.clientId
                    status = ex.toString()
                    errorCode = ex.status().toLong()
                    campaignType=message.type
                    userId=message.userId
                    serviceProviderId=credential.id
                    serviceProvider=credential.name
                }
                throw FcmFailureException(notificationError)
                //toFcmFailureKafka(notificationError)
            } catch (ex: FcmFailureException) {
                service.updateStatus(mongoFcmId, FcmMessageStatus.ERROR, message.clientId,message.type)
                logger.info("Fcm Failure Exception with status code $statusCode message ${ex.message}")
                var notificationError = NotificationError()
                with(notificationError) {
                    this.message = ex.message
                    to = message.to
                    clientId = message.clientId
                    status = ex.toString()
                    errorCode = statusCode?.toLong()
                    campaignType=message.type
                    userId=message.userId
                    serviceProviderId=credential.id
                    serviceProvider=credential.name
                }
                throw FcmFailureException(notificationError)
                //toFcmFailureKafka(notificationError)

            } catch (ex: Exception) {
                service.updateStatus(mongoFcmId, FcmMessageStatus.ERROR, message.clientId,message.type)
                logger.info("Exception in sending fcm message $ex")
                var notificationError = NotificationError()
                with(notificationError) {
                    this.message = ex.message
                    to = message.to
                    clientId = message.clientId
                    status = ex.toString()
                    errorCode = statusCode?.toLong()
                    campaignType=message.type
                    userId=message.userId
                    serviceProviderId=credential.id
                    serviceProvider=credential.name
                }
                throw FcmFailureException(notificationError)
                //toFcmFailureKafka(notificationError)
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
}

class NotificationError {
    var to: String? = null
    var status: String? = null
    var message: String? = null
    var clientId: Long? = null
    var errorCode: Long? = null
    var campaignType:String?=null
    var userId:String?=null
    var serviceProvider:String?=null
    var serviceProviderId:Long?=null
}

class TestMessage {
    lateinit var token: String
    lateinit var data: HashMap<String, String>
}


