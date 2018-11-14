package com.und.report.web.controller


import com.und.report.web.model.CampaignReached
import com.und.report.web.model.FunnelReport
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController("/report/campaign")
@RequestMapping("/report/campaign")
class CampaignPageReportController {


    @GetMapping("/funnel")
    fun countTrend(@RequestParam(name = "campaignId") campaignid: Long): List<FunnelReport.FunnelStep> {

        return emptyList()
    }

    @GetMapping("/reach")
    fun reachedCount(@RequestParam(name = "campaignId") campaignid: Long): CampaignReached {

        return CampaignReached()
    }


}