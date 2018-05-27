package com.und.model.utils.fcm

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test

class FcmMessageTest {
    @Test
    fun testNotificationJson() {
        var notification = FcmMessage(to="amit", notification = NotificationPayloadWeb(title = "Hello World"))
        println(jacksonObjectMapper().writeValueAsString(notification))
        notification = FcmMessage(registration_ids = listOf("amit", "lamba"), notification = NotificationPayloadWeb(title = "Hello World"))
        println(jacksonObjectMapper().writeValueAsString(notification))
        notification = FcmMessage(registration_ids = listOf("amit", "lamba"),
                notification = NotificationPayloadWeb(title = "Hello World", body = "Hello World Body"),
                data = mapOf(Pair("abc", "xyx"),
                        Pair("xyz", "abc")))
        println(jacksonObjectMapper().writeValueAsString(notification))
    }
}