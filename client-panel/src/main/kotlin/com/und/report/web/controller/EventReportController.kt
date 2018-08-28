package com.und.report.web.controller


import com.und.report.web.model.EventReport
import com.und.report.web.model.EventReport.EventCount
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController("/report/event")
class EventReportController {

    /**
     * @param groupBy name of property on which count of users will be split
     */
    @GetMapping("/eventcount")
    //count of events by event property and user property (based on property type chart will be split)
    // e.g.  number 20-25(count=2), 25-40(count=3), 40-45(count=4)
    // e.g.  dates 1st Aug-1stsep(count=5), and so on
    // e.g. if type is string(name of product)  we will show count on every string
    //by gender count
    //by country, state, city
    //by browser, os, device, appversion
    fun countTrendC(@RequestParam(name = "ftr") requestFilter: EventReport.EventReportFilter,
                    @RequestParam(name = "entityType") entityType: EventReport.EntityType,
                    @RequestParam(name = "groupby", defaultValue = "os") groupBy: String): List<EventCount> {

        //FIXME call service methods
        return emptyList()
    }


    //count of events/users on date, week, months (count, 28-aug-2018), (count, week(25-31 august 2018), (count, month(1-31 august 2018))
    @GetMapping("/trendBytimePeriod")
    fun timePeriodTrend(@RequestParam(name = "ftr") requestFilter: EventReport.EventReportFilter,
                        @RequestParam(name = "period") period: EventReport.PERIOD): List<EventReport.EventPeriodCount> {
        //FIXME call service methods
        return emptyList()

    }


    //frequency chart (count of users  group by count of events )
    @GetMapping("/eventUserTrend")
    fun eventUserTrend(@RequestParam(name = "ftr") requestFilter: EventReport.EventReportFilter): List<EventReport.EventUserFrequency> {
        return emptyList()
    }


    //count of events based on time range e.g how many events on 1-2 , how many on 2-3 pm etc.
    @GetMapping("/eventTimeTrend")
    fun eventTimeTrend(@RequestParam(name = "ftr") requestFilter: EventReport.EventReportFilter): List<EventReport.EventTimeFrequency> {
        return emptyList()
    }

    //aggregate on a property on time scale of days, week, month  if property is amount than revenue report,
    fun aggregateTrend(@RequestParam(name = "ftr") requestFilter: EventReport.EventReportFilter,
                       @RequestParam(name = "period") period: EventReport.PERIOD,
                       @RequestParam(name = "aggregateOn", defaultValue = "amount") aggregateOn: String): List<EventReport.Aggregate> {

        //FIXME call service methods
        return emptyList()
    }
}