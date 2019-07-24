package com.und.web.controller


import com.und.service.CampaignReachedService
import com.und.web.model.CampaignReached
import com.und.web.model.FunnelReport
import com.und.security.utils.AuthenticationUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController("campaignPageReportController")
@RequestMapping("/report/campaign")
class CampaignPageReportController {

    @Autowired
    private lateinit var campaignReachedService: CampaignReachedService

    @PreAuthorize(value = "hasRole('ROLE_ADMIN')")
    @GetMapping("/funnel")
    fun countTrend(@RequestParam(name = "campaignId") campaignid: Long): List<FunnelReport.FunnelStep> {

        return emptyList()
    }
    @PreAuthorize(value = "hasRole('ROLE_ADMIN')")
    @GetMapping("/reach")
    fun reachedCount(@RequestParam(name = "campaignId") campaignid: Long): CampaignReached {
        val clientId = AuthenticationUtils.retrieveClientId()
        return campaignReachedService.getCampaignReachability(campaignid,clientId)
    }


}