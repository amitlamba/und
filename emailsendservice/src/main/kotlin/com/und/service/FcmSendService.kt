package com.und.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.und.model.utils.fcm.FcmMessage
import com.und.model.utils.fcm.NotificationPayloadWeb
import feign.FeignException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class FcmSendService {

    @Autowired
    private lateinit var fcmFeignClient: FcmFeignClient


    fun sendMessage(clientId: Long, authKey: String, message: FcmMessage): ResponseEntity<Any?>? {
        try {
            return fcmFeignClient.pushMessage("key=" + authKey, jacksonObjectMapper().writeValueAsString(message))
        } catch (ex: FeignException) {
            println("Feign Error Code: ${ex.status()}, Message: ${ex.message}")
            ex.printStackTrace()
        }
        return null
    }

    fun sendMessage(clientId: Long, authKey: String, notificationTitle: String, notificationText: String,
                    to: String): ResponseEntity<Any?>? {
        val message = FcmMessage(to = to, notification = NotificationPayloadWeb(title = notificationTitle, body = notificationText))
        return sendMessage(clientId, authKey, message)
    }


    /*
    Send Message to a user

    POST https://fcm.googleapis.com/v1/projects/myproject-b5ae1/messages:send HTTP/1.1

    Content-Type: application/json
    Authorization: Bearer ya29.ElqKBGN2Ri_Uz...HnS_uNreA

    {
      "message":{
        "token" : "bk3RNwTe3H0:CI2k_HHwgIpoDKCIZvvDMExUdFQ3P1...",
        "notification" : {
          "body" : "This is an FCM notification message!",
          "title" : "FCM Message",
          }
       }
    }
     */

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
}