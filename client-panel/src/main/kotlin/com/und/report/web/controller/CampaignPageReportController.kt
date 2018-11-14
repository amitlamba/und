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

    /*
    * db.getCollection("3_eventUser3").aggregate([
{$match:{"standardInfo.gender":"Male"}},
{$facet:{
"email":[
{$match:{$nor:[{"identity.email": null },{"communication.email.dnd": true }]}},{$count:"count"}
],
"mobile":[
{$match:{$nor:[{"identity.mobile": null },{"communication.mobile.dnd": true }]}},{$count:"count"}
],
"webpush":[
{$match:{$nor:[{"identity.webpush": null },{"communication.email.dnd": true }]}},{$count:"count"}
],
"android":[
{$match:{$nor:[{"identity.android": null },{"communication.android.dnd": true }]}},{$count:"count"}
],
"ios":[
{$match:{$nor:[{"identity.ios": null },{"communication.ios.dnd": true }]}},{$count:"count"}
]
}},
{$project:{"emailcount":"$email.count","androidcount":"$android.count","mobilecount":"$mobile.count","ioscount":"$ios.count","webpushcount":"$webpush.count"}}
])
    * */
    @GetMapping("/reach")
    fun reachedCount(@RequestParam(name = "campaignId") campaignid: Long): CampaignReached {

        return CampaignReached()
    }


}