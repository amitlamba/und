package com.und.scheduler

import com.und.job.CampaignJobService
import com.und.job.LiveSegmentJobService
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class LiveSegmentJob: Job {

    @Autowired
    lateinit var jobService: LiveSegmentJobService

    override fun execute(context: JobExecutionContext) {
        val clientId = context.jobDetail.jobDataMap["clientId"] as String
        val segmentId = context.jobDetail.jobDataMap["segmentId"] as String
        val startEventId = context.jobDetail.jobDataMap["startEventId"] as String
        val startEventName = context.jobDetail.jobDataMap["startEventName"] as String
        val startEventTime = context.jobDetail.jobDataMap["startEventTime"] as String
        val userId = context.jobDetail.jobDataMap["userId"] as String

        this.jobService.executeJob(LiveSegmentUserCheck(clientId, segmentId, startEventId, startEventName, startEventTime, userId))
    }
}

data class LiveSegmentUserCheck(var clientId: String, var segmentId: String, var startEventId: String,
                                var startEventName: String, var startEventTime: String, var userId: String)