package com.und.service

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.und.model.utils.fcm.FcmMessage
import com.und.model.utils.fcm.NotificationPayloadWeb
import feign.FeignException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

@Service
class FcmSendService {

    @Autowired
    private lateinit var fcmFeignClient: FcmFeignClient
    @Autowired
    private lateinit var service: FcmHelperService
    @Autowired
    private lateinit var objectMapper: ObjectMapper

    fun sendMessage(clientId: Long, authKey: String, message: FcmMessage): ResponseEntity<Any?>? {
        try {
            return fcmFeignClient.pushMessage("key=" + authKey, jacksonObjectMapper().writeValueAsString(message))
        } catch (ex: FeignException) {
            println("Feign Error Code: ${ex.status()}, Message: ${ex.message}")
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
            println(sb.toString())
            var accessToken = getAccessToken(sb.toString())
            println("access token $accessToken")
            println(send(message, accessToken))
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
        println("message is $out")
        outputstream.write(out.toByteArray())
        outputstream.flush()
        outputstream.close()
        print(conn.responseMessage)
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

    fun sendMessage(message: com.und.model.utils.FcmMessage) {
        var fcmMessageToSend = service.buildFcmMessage(message)
        service.saveInMongo(fcmMessageToSend)
        var credential = service.getCredentials(message.clientId)
        if (credential == null) {//throw excception credential not exists
        }
        var credentialMap: HashMap<String, String>
        credentialMap = parseStringToMap(credential?.credentialsMap!!)
        var serverKey = credentialMap.get("apiKey")!!
        sendMessageToFcm(fcmMessageToSend, serverKey)
    }

    private fun sendMessageToFcm(fcmMessage: com.und.model.mongo.FcmMessage, serverKey: String) {
        var response = fcmFeignClient.pushMessage(serverKey, objectMapper.writeValueAsString(fcmMessage))
        var statusCode = response.statusCodeValue
        if (statusCode == 200) {
            //update mongo state to send
        } else {
            //throw exception
        }
    }

    fun parseStringToMap(jsonString: String): HashMap<String, String> {
        var hashMap = HashMap<String, String>()
        var jsonNode: JsonNode = objectMapper.readTree(jsonString)
        var entityMap = jsonNode.fields()
        entityMap.forEach {
            hashMap.put(it.key, it.value.toString())
        }
        return hashMap
    }
}

class TestMessage {
    lateinit var token: String
    lateinit var data: HashMap<String, String>
}


