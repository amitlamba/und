package com.und.report.web.controller


import com.und.report.service.CampaignReachedService
import com.und.report.web.model.CampaignReached
import com.und.report.web.model.FunnelReport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController("campaignPageReportController")
@RequestMapping("/report/campaign")
class CampaignPageReportController {

    @Autowired
    private lateinit var campaignReachedService: CampaignReachedService

    @GetMapping("/funnel")
    fun countTrend(@RequestParam(name = "campaignId") campaignid: Long): List<FunnelReport.FunnelStep> {

        return emptyList()
    }

    @GetMapping("/reach")
    fun reachedCount(@RequestParam(name = "campaignId") campaignid: Long): CampaignReached {
        return campaignReachedService.getCampaignReachability(campaignid)
    }


}