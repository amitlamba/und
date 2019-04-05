package com.und.report.web.controller


import com.fasterxml.jackson.databind.ObjectMapper
import com.und.model.IncludeUsers
import com.und.report.service.UserEventAnalyticsService
import com.und.report.web.model.AggregateBy
import com.und.report.web.model.EventReport
import com.und.report.web.model.EventReport.EventCount
import com.und.report.web.model.GroupBy
import com.und.report.web.model.Reachability
import com.und.web.model.EventUser
import com.und.web.model.GlobalFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController("/report/event")
@RequestMapping("/report/event")
class EventReportController {

    @Autowired
    private lateinit var userAnalyticsService: UserEventAnalyticsService
    /**
     * @param groupBy name of property on which count of users will be split
     */
    @PostMapping("/eventcount")
    //count of events by event property and user property (based on property type chart will be split)
    // e.g.  number 20-25(count=2), 25-40(count=3), 40-45(count=4)
    // e.g.  dates 1st Aug-1stsep(count=5), and so on
    // e.g. if type is string(name of product)  we will show count on every string
    //by gender count
    //by country, state, city
    //by browser, os, device, appversion


    //Days would be last X days only (no concept of last month i.e. days from last 60 to last 30)
    //EntityType is for deciding properties or count?
    //Users/Distinct(Users)? -> In event trend users, in user trend distinct(user)
    //All the properties allowed in groupby/filtering (some could be nested, so have to categorize properties in code and then write queries accordingly)
    //Different logic for different type of groupby
    //select timeZoneId, count(id/distinct(userId)) from event group by timeZoneId where clientId = 3 and creationTime > -100 days and name = 'charged'
    //              and userId in (users in segment)
    //              <more property filters>
    fun countTrend(@RequestParam(name = "entityType") entityType: EventReport.EntityType,
                   @RequestBody propFilter: List<GlobalFilter>,
                   requestFilter: EventReport.EventReportFilter ,
                   groupBy: GroupBy): List<EventCount> {

        requestFilter.propFilter=propFilter
        return userAnalyticsService.countTrend(requestFilter, entityType, groupBy, IncludeUsers.KNOWN)
    }


    @PostMapping("/eventReachability")
    fun eventReachability(@RequestParam(name = "entityType") entityType: EventReport.EntityType,
                   @RequestBody propFilter: List<GlobalFilter>,
                   requestFilter: EventReport.EventReportFilter ,
                   groupBy: GroupBy): Reachability {

        requestFilter.propFilter=propFilter
//        return userAnalyticsService.countTrend(requestFilter, entityType, groupBy)
        return userAnalyticsService.eventReachability(requestFilter,entityType,groupBy,IncludeUsers.KNOWN)

    }

    //count of events/users on date, week, months (count, 28-aug-2018), (count, week(25-31 august 2018), (count, month(1-31 august 2018))
    @PostMapping("/trendBytimePeriod")
    //EntityType is missing in request parameters?
    //Users/Distinct(Users)? -> In event trend users, in user trend distinct(user)
    //same as above mostly
    fun timePeriodTrend(@RequestParam(name = "entityType") entityType: EventReport.EntityType,
                        @RequestBody propFilter: List<GlobalFilter>,
                        @RequestParam(name = "period") period: EventReport.PERIOD, requestFilter: EventReport.EventReportFilter): List<EventReport.EventPeriodCount> {
        requestFilter.propFilter=propFilter
        return userAnalyticsService.timePeriodTrend(requestFilter, entityType, period,IncludeUsers.KNOWN)
    }


    //frequency chart (count of users  group by count of events )
    @PostMapping("/eventUserTrend")
    //For last 100 days?
    //Users/Distinct(Users)?
    //All possible count of events to be shown, no ranging to be done (assumption is there won't be more than 10-20 possible counts)
    fun eventUserTrend(requestFilter: EventReport.EventReportFilter,
                       @RequestBody propFilter: List<GlobalFilter>): List<EventReport.EventUserFrequency> {

        requestFilter.propFilter=propFilter
        return userAnalyticsService.eventUserTrend(requestFilter,IncludeUsers.KNOWN)
    }


    //count of events based on time range e.g how many events on 1-2 , how many on 2-3 pm etc.
    @PostMapping("/eventTimeTrend")
    //Period is always houly?
    //For last 100 days?
    fun eventTimeTrend(requestFilter: EventReport.EventReportFilter,
                       @RequestBody propFilter: List<GlobalFilter>): List<EventReport.EventTimeFrequency> {

        requestFilter.propFilter=propFilter
        return userAnalyticsService.eventTimeTrend(requestFilter,IncludeUsers.KNOWN)
    }

    //aggregate on a property on time scale of days, week, month  if property is amount than revenue report,
    @PostMapping("/eventAggregateTrend")
    fun aggregateTrend(@RequestParam(name = "period") period: EventReport.PERIOD,
                       aggregateBy: AggregateBy, requestFilter: EventReport.EventReportFilter,
                       @RequestBody propFilter: List<GlobalFilter>): List<EventReport.Aggregate> {
        requestFilter.propFilter=propFilter
        return userAnalyticsService.aggregateTrend(requestFilter, period, aggregateBy,IncludeUsers.KNOWN)
    }
}