package com.und.report.web.controller

import com.und.model.IncludeUsers
import com.und.report.service.UserEventAnalyticsService
import com.und.report.web.model.*
import com.und.security.utils.AuthenticationUtils
import com.und.web.model.EventUser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*


@RestController("/dashboard")
@RequestMapping("/dashboard")
class DashBoardController {

    @Autowired
    private lateinit var userAnalyticsService: UserEventAnalyticsService

    /**
     * @param groupBy name of property on which count of users will be split
     */
    @PreAuthorize(value = "hasRole('ROLE_ADMIN')")
    @GetMapping("/liveusers")
    //Users in a particular segment? Postgres? [Same in all the apis] [new method in SegmentService]
    //All the properties allowed in groupBy (some could be nested, so have to categorize properties in code and then write queries accordingly)
    //select timeZoneId, count(distinct(userId)) from event group by timeZoneId where clientId = 3 and creationTime > -5 mins
    //              and userId in (users in segment)
    //groupBy = event-name, country, state, city, os, browser, device
    fun liveUsers(@RequestParam("segmentid", required = true, defaultValue = "1") segmentId: Long,
                   @RequestParam("interval", required = true, defaultValue = "5") interval: Long, groupBy: GroupBy): List<UserCountForProperty> {
        val clientId = AuthenticationUtils.retrieveClientId()
        return userAnalyticsService.liveUsers(segmentId, groupBy, interval,IncludeUsers.KNOWN,clientId)
    }

    @PreAuthorize(value = "hasRole('ROLE_ADMIN')")
    @GetMapping("/liveusertrend")
    //Compute on runtime through query or keep pre-computed data? Think about case where data is asked for many data, could it be large?
    //interval would be in minutes only (min val = 5, max val = 24*60)
    //in output as well, time is in minutes from 5 to 24*60
    //select timeInterval, count(distinct(userId)) from
    //          (select userId, fn(creationTime) as timeInterval from event  where clientId = 3 and creationTime in (range of dates) and userId in (users in segment))
    //      group by timeInterval
    fun liveUserTrend(@RequestParam("segmentid", required = true, defaultValue = "1") segmentId: Long,
                   @RequestParam("dates", required = true, defaultValue = "today") dates: List<String>,
                   @RequestParam("interval", required = true, defaultValue = "5") interval: Long):List<UserCountTrendForDate> {
        val clientId = AuthenticationUtils.retrieveClientId()
        return userAnalyticsService.liveUserTrend(segmentId, dates, interval,IncludeUsers.KNOWN,clientId)
    }

    /**
     * Types of users: new, existing
     */
    @PreAuthorize(value = "hasRole('ROLE_ADMIN')")
    @GetMapping("/liveusertypetrend")
    fun liveUserByTypeTrend(@RequestParam("segmentid", required = true, defaultValue = "1") segmentId: Long,
                   @RequestParam("dates", required = true, defaultValue = "today") date: List<String>,
                   @RequestParam("interval", required = true, defaultValue = "5") interval: Long):List<UserTypeTrendForDate> {
        val clientId = AuthenticationUtils.retrieveClientId()
        return userAnalyticsService.liveUserByTypeTrend(segmentId, date, interval,IncludeUsers.KNOWN,clientId)
    }

    @PreAuthorize(value = "hasRole('ROLE_ADMIN')")
    @GetMapping("/usercountbyevents")
    //select name, count(distinct(userId)).....
    fun userCountByEvent(@RequestParam("segmentid", required = true, defaultValue = "1") segmentId: Long,
                   @RequestParam("dates", required = true, defaultValue = "today") dates: List<String>):List<UserCountByEventForDate> {
        val clientId = AuthenticationUtils.retrieveClientId()
        return userAnalyticsService.userCountByEvent(segmentId, dates,IncludeUsers.KNOWN,clientId)
    }

    @PreAuthorize(value = "hasRole('ROLE_ADMIN')")
    @GetMapping("/samepleusersbyevent")
    //FIXME call service methods and data types
    fun sampleUsersByEvent(@RequestParam("segmentid", required = true, defaultValue = "1") segmentId: Long,
                         @RequestParam("date", required = true, defaultValue = "today") date: String):List<EventUser> {
        val clientId = AuthenticationUtils.retrieveClientId()
        return emptyList<EventUser>()

    }

}