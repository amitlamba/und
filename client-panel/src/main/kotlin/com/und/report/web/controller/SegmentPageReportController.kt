package com.und.report.web.controller


import com.und.report.web.model.Reachability
import com.und.web.model.Campaign
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController("/report/segment")
@RequestMapping("/report/segment")
class SegmentPageReportController {


    @GetMapping("/reachability")
    fun reachability(@RequestParam(name = "segmentid") segmentId: Long): Reachability {

        return Reachability()
    }


    @GetMapping("/campaigns")
    fun campaigns(@RequestParam(name = "segmentid") segmentId: Long): List<Campaign> {
        return emptyList()
    }


}