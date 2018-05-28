package com.und.service

import com.und.model.mongo.EventUser
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod


@FeignClient(name = "client-panel", value = "client-panel")
interface SegmentUserServiceClient {

    //FIXME define a new role and key here from config
    @GetMapping(value = ["/segment/users/{segmentId}"], consumes = ["application/json"]
            , headers = ["Authorization=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJJZCI6IjIiLCJjbGllbnRJZCI6IjIiLCJyb2xlcyI6WyJST0xFX1VTRVIiLCJST0xFX0FETUlOIl0sImNyZWF0ZWQiOjE1MjY3OTc5MTU3NDksImV4cCI6MTUyNzQwMjcxNX0.FPgI6B-8gZxQkYmUK_pfJBJUjQNFdwyMoVqIVphT3oP_KknDmwTeu6i7SldrnDv9gGZy8bkfNYYpyxUTD4UeXA"])
    fun users(@PathVariable("segmentId") segmentId: Long): List<EventUser>


}