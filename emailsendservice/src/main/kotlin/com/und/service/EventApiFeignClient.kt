package com.und.service

import com.und.model.utils.eventapi.Event
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(name = "event-api")
interface EventApiFeignClient {
    @PostMapping("/push/event",consumes = arrayOf("application/json"),produces = arrayOf("application/json"))
    fun pushEvent(@RequestHeader("Authorization")auth:String?,@RequestBody event: Event)
}