package com.und.report.web.controller

import com.und.report.web.model.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController("/dashboard")
class DashBoardController {

    /**
     * @param groupBy name of property on which count of users will be split
     */
    @GetMapping("/trendcount")
    fun trendCount(@RequestParam("segmentid", required = true, defaultValue = "1") segmentId: Long,
                   @RequestParam("groupby", required = true, defaultValue = "os") groupBy: String): List<TrendCount> {

        //FIXME call service methods
        return emptyList()
    }


    @GetMapping("/trendchart")
    fun trendChart(@RequestParam("segmentid", required = true, defaultValue = "1") segmentId: Long,
                   @RequestParam("dates", required = true, defaultValue = "today") date: List<String>,
                   @RequestParam("interval", required = true, defaultValue = "5") interval: Long):List<TrendTimeSeries> {
        //FIXME call service methods
        return emptyList()
    }

    @GetMapping("/newvsexisting")
    fun newVsExisting(@RequestParam("segmentid", required = true, defaultValue = "1") segmentId: Long,
                   @RequestParam("dates", required = true, defaultValue = "today") date: List<String>,
                   @RequestParam("interval", required = true, defaultValue = "5") interval: Long):List<UserCountTimeSeries> {
        //FIXME call service methods
        return emptyList()
    }


    @GetMapping("/usercountbyevents")
    fun userCountByEvent(@RequestParam("segmentid", required = true, defaultValue = "1") segmentId: Long,
                   @RequestParam("dates", required = true, defaultValue = "today") date: List<String>):List<UserCountByEventTimeSeries> {

        return emptyList()
    }


    @GetMapping("/samepleusersbyevent")
    //FIXME call service methods and data types
    fun sampleUsersByEvent(@RequestParam("segmentid", required = true, defaultValue = "1") segmentId: Long,
                         @RequestParam("dates", required = true, defaultValue = "today") date: List<String>):List<String> {

        return emptyList()
    }



}