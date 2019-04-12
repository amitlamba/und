package com.und.service

import com.und.model.mongo.EventUser
import com.und.model.utils.IncludeUsers
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.*


@FeignClient(name = "client-panel", value = "client-panel")
//@FeignClient(name = "client-panel",url = "http://userndot.com:9201")
interface SegmentUserServiceClient {

    //FIXME define a new role and key here from config
    //, headers = ["Authorization=eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsInVzZXJJZCI6IjIiLCJjbGllbnRJZCI6IjIiLCJyb2xlcyI6WyJST0xFX1VTRVIiLCJST0xFX0FETUlOIl0sImNyZWF0ZWQiOjE1MjY3OTc5MTU3NDksImV4cCI6MTUyNzQwMjcxNX0.FPgI6B-8gZxQkYmUK_pfJBJUjQNFdwyMoVqIVphT3oP_KknDmwTeu6i7SldrnDv9gGZy8bkfNYYpyxUTD4UeXA"]
    @GetMapping(value = ["/segment/users/{segmentId}/{clientId}"], consumes = ["application/json"])
    fun users(@PathVariable("segmentId") segmentId: Long, @PathVariable("clientId") clientId: Long,  @RequestHeader("Authorization") token: String,@RequestParam("include")includeUsers:IncludeUsers,@RequestParam("fromCampaign")fromCampaign:String): List<EventUser>


    @GetMapping("/report/funnel/winner/template")
    fun getWinnerTemplate(@RequestParam campaignId:Long,@RequestParam clientId: Long,@RequestHeader("Authorization")token: String):Long
}