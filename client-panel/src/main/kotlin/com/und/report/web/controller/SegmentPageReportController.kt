package com.und.report.web.controller


import com.und.model.IncludeUsers
import com.und.report.model.SegmentTrendCount
import com.und.report.service.ReachabilityService
import com.und.report.web.model.Reachability
import com.und.security.utils.AuthenticationUtils
import com.und.service.CampaignService
import com.und.web.model.Campaign
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@RestController("/report/segment")
@RequestMapping("/report/segment")
class SegmentPageReportController {

    @Autowired
    private lateinit var campaignService: CampaignService
    @Autowired
    private lateinit var reachabilityService: ReachabilityService

    @GetMapping("/reachability")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun reachability(@RequestParam(name = "segmentid") segmentId: Long,request: HttpServletRequest): Reachability {
        val includeUsers=IncludeUsers.valueOf(request.getParameter("include")?:"ALL")
        return reachabilityService.getReachabilityBySegmentId(segmentId,includeUsers)
    }


    @GetMapping("/campaigns")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun campaigns(@RequestParam(name = "segmentid") segmentId: Long): List<Campaign> {
        return campaignService.getListOfCampaign(segmentId)
    }

    @GetMapping("/set/{segmentId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun setSegmentReachability(@PathVariable (required = true)segmentId: Long){
        val clientId = AuthenticationUtils.clientID ?: throw AccessDeniedException("")
        reachabilityService.setReachabilityOfSegmentToday(segmentId,clientId)
    }

    @GetMapping("/get/{segmentId}/{date}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getSegmentReachabilityByDate(@PathVariable (required = true)date: String,@PathVariable(required = true) segmentId: Long):Map<String,Int>?{
        return reachabilityService.getReachabilityOfSegmentByDate(segmentId,date)
    }

    @GetMapping("/get/{segmentId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    fun getSegmentReachabilityByDateRangle(@RequestParam (value = "start" ,required = true)startDate: String,
                                           @RequestParam (value = "end" ,required = true)endDate: String,
                                           @PathVariable (required = true) segmentId: Long):List<SegmentTrendCount>{
        val clientId = AuthenticationUtils.clientID ?: throw AccessDeniedException("Access Denied.")
        val isLive=reachabilityService.checkTypeOfSegment(clientId,segmentId)
        if(isLive) return emptyList()
        return reachabilityService.getReachabilityOfSegmentByDateRange(clientId,segmentId,startDate,endDate)
    }

}