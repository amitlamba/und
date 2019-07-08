package com.und.service

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name ="report-service",value = "report-service")
interface ReportServiceFeignClient {

    @GetMapping(value = ["/report/funnel/winner/template"])
    fun getWinnerTemplate(@RequestParam("campaignId") campaignId:Long, @RequestParam("clientId") clientId: Long, @RequestHeader("Authorization")token: String, @RequestParam("include") include:String):Long

}