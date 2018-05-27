package com.und.service

import com.und.model.Subscription
import nl.martijndwars.webpush.Notification
import nl.martijndwars.webpush.PushService



class WebPushSendService {
    /** The Time to live of GCM notifications  */
    private val TTL = 255

    fun sendPushMessage(sub: Subscription, payload: ByteArray) {

        // Figure out if we should use GCM for this notification somehow
        val useGcm = shouldUseGcm(sub)
        val notification: Notification
        val pushService: PushService

        if (useGcm) {
            // Create a notification with the endpoint, userPublicKey from the subscription and a custom payload
            notification = Notification(
                    sub.endPoint,
                    sub.getUserPublicKey(),
                    sub.getAuthAsBytes(),
                    payload
            )

            // Instantiate the push service, no need to use an API key for Push API
            pushService = PushService()
        } else {
            // Or create a GcmNotification, in case of Google Cloud Messaging
            notification = Notification(
                    sub.endPoint,
                    sub.getUserPublicKey(),
                    sub.getAuthAsBytes(),
                    payload,
                    TTL
            )

            // Instantiate the push service with a GCM API key
            pushService = PushService("gcm-api-key")
        }

        // Send the notification
        pushService.send(notification)
    }

    private fun shouldUseGcm(sub: Subscription): Boolean {
        //sub.key = "122"
        return false
    }

}