package com.und.report.web.controller


import com.und.report.service.ReachabilityService
import com.und.report.web.model.Reachability
import com.und.service.CampaignService
import com.und.web.model.Campaign
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController("/report/segment")
@RequestMapping("/report/segment")
class SegmentPageReportController {

    @Autowired
    private lateinit var campaignService: CampaignService
    @Autowired
    private lateinit var reachabilityService: ReachabilityService

    @GetMapping("/reachability")
    fun reachability(@RequestParam(name = "segmentid") segmentId: Long): Reachability {
        return reachabilityService.getReachabilityBySegmentId(segmentId)
    }


    @GetMapping("/campaigns")
    fun campaigns(@RequestParam(name = "segmentid") segmentId: Long): List<Campaign> {
        return campaignService.getListOfCampaign(segmentId)
    }


}