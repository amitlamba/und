package com.und.report.web.controller

import com.und.report.web.model.*
import com.und.web.model.EventUser
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController


@RestController("/dashboard")
class DashBoardController {

    /**
     * @param groupBy name of property on which count of users will be split
     */
    @GetMapping("/trendcount")
    //Users in a particular segment? Postgres? [Same in all the apis] [new method in SegmentService]
    //All the properties allowed in groupBy (some could be nested, so have to categorize properties in code and then write queries accordingly)
    //select timeZoneId, count(distinct(userId)) from event group by timeZoneId where clientId = 3 and creationTime > -5 mins
    //              and userId in (users in segment)
    fun trendCount(@RequestParam("segmentid", required = true, defaultValue = "1") segmentId: Long,
                   @RequestParam("groupby", required = true, defaultValue = "os") groupBy: String,
                   @RequestParam("interval", required = true, defaultValue = "5") interval: Long): List<TrendCount> {

        //FIXME call service methods
        return emptyList()
    }


    @GetMapping("/trendchart")
    //Compute on runtime through query or keep pre-computed data? Think about case where data is asked for many data, could it be large?
    //interval would be in minutes only (min val = 5, max val = 24*60)
    //in output as well, time is in minutes from 5 to 24*60
    //select timeInterval, count(distinct(userId)) from
    //          (select userId, fn(creationTime) as timeInterval from event  where clientId = 3 and creationTime in (range of dates) and userId in (users in segment))
    //      group by timeInterval
    fun trendChart(@RequestParam("segmentid", required = true, defaultValue = "1") segmentId: Long,
                   @RequestParam("dates", required = true, defaultValue = "today") date: List<String>,
                   @RequestParam("interval", required = true, defaultValue = "5") interval: Long):List<TrendTimeSeries> {
        //FIXME call service methods
        return emptyList()
    }

    @GetMapping("/newvsexisting")
    //same as above
    fun newVsExisting(@RequestParam("segmentid", required = true, defaultValue = "1") segmentId: Long,
                   @RequestParam("dates", required = true, defaultValue = "today") date: List<String>,
                   @RequestParam("interval", required = true, defaultValue = "5") interval: Long):List<UserCountTimeSeries> {
        //FIXME call service methods
        return emptyList()
    }


    @GetMapping("/usercountbyevents")
    //select name, count(distinct(userId)).....
    fun userCountByEvent(@RequestParam("segmentid", required = true, defaultValue = "1") segmentId: Long,
                   @RequestParam("dates", required = true, defaultValue = "today") date: List<String>):List<UserCountByEventTimeSeries> {

        return emptyList()
    }


    @GetMapping("/samepleusersbyevent")
    //FIXME call service methods and data types
    fun sampleUsersByEvent(@RequestParam("segmentid", required = true, defaultValue = "1") segmentId: Long,

                         @RequestParam("date", required = true, defaultValue = "today") date: String):List<EventUser> {
        return emptyList<EventUser>()

    }



}