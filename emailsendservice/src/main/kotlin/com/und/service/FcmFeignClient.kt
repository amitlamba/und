package com.und.service

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/*
url: 'https://fcm.googleapis.com/fcm/send',
    method: 'POST',
    headers: {
      'Content-Type' :' application/json',
      'Authorization': 'key=AI...8o'
    },
    body: JSON.stringify(
      { "data": {
        "message": message
      },
        "to" : deviceId
      }
    )
 */

@FeignClient(name = "fcm",url = "https://fcm.googleapis.com/fcm")
interface FcmFeignClient {

    @PostMapping(value = ["/send"], consumes = ["application/json"])
    fun pushMessage(@RequestHeader("Authorization") authKeyValue: String,
                    @RequestBody requestBodyString: String): ResponseEntity<Any?>

}