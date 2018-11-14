package com.und.report.web.controller


import com.und.report.service.ReachabilityService
import com.und.report.web.model.Reachability
import com.und.service.CampaignService
import com.und.web.model.Campaign
import org.bson.types.ObjectId
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
    private lateinit var reachabilityService:ReachabilityService

    /*
    * db.getCollection("3_eventUser3").aggregate([
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
    @GetMapping("/reachability")
    fun reachability(@RequestParam(name = "segmentid") segmentId: Long): Reachability {
        return reachabilityService.getReachabilityBySegmentId(segmentId)
    }


    @GetMapping("/campaigns")
    fun campaigns(@RequestParam(name = "segmentid") segmentId: Long): List<Campaign> {
        return campaignService.getListOfCampaign(segmentId)
    }


}