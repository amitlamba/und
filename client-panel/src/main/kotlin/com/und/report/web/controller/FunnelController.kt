package com.und.report.web.controller

import com.und.report.service.FunnelReportService
import com.und.report.web.model.FunnelReport
import com.und.report.web.model.FunnelStepAndFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController("/report/funnel")
@RequestMapping("/report/funnel")
class FunnelController {

    @Autowired
    private lateinit var funnelReportService: FunnelReportService;

/*
        for a segment lets say all users and for a time period lets say 100 days

    choose steps

    step1 -> searched a product
    step 2 -> viewd product
    step 3 -> add to cart
    step4 (conversion step) -> charged

    question 2 -> what would be the time for conversion to be considered (5-7 days but option to change it)
    answer: default is 5 days for completing all steps, option to change this value

    question 2 -> should steps be considered in order or out of order as well.
    default- it should be in order but there can be steps other than specified in between
    user can opt for strict order, in which case there cant be any other step in between.

    output of above is a bar chart
    user can split bar chart by a property


    100->25->13->5
 */


    @PostMapping("/funnel")
    fun funnel(@RequestBody(required = true) body:FunnelStepAndFilter,
               funnelFilter: FunnelReport.FunnelReportFilter): List<FunnelReport.FunnelStep> {
        if((funnelFilter.splitProperty?.isEmpty())?:true) funnelFilter.splitProperty=null
        funnelFilter.steps=body.steps
        funnelFilter.filters=body.filters
        return funnelReportService.funnel(funnelFilter)
    }

    @PreAuthorize("hasRole('ROLE_SYSTEM')")
    @GetMapping("/winner/template")
    fun winnerTemplate(@RequestParam("")campaignId:Long,@RequestParam("clientId")clientId:Long):Long{
        //find variant from campaign
        //get all variant result
        //use template id as filter along with campaign id.
        val variant1= listOf(FunnelReport.FunnelStep(FunnelReport.Step(1,"event"),0,"all"))
        val variant2= listOf(FunnelReport.FunnelStep(FunnelReport.Step(1,"event"),0,"all"))
        var variant1StepsCount=variant1.find {
            it.step.order == variant1.size
        }
        var variant2StepsCount=variant2.find {
            it.step.order == variant2.size
        }
        //find winner update in table.
    }
}