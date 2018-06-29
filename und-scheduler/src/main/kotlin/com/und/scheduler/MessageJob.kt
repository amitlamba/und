package com.und.scheduler


import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import com.und.job.MessageJobService
import com.und.util.JobUtil
import org.quartz.Scheduler
import org.quartz.impl.matchers.GroupMatcher

@Component
class MessageJob : Job {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var  jobService : MessageJobService

    @Autowired
    protected lateinit var scheduler: Scheduler

    override fun execute(context: JobExecutionContext) {
        val clientId = context.jobDetail.jobDataMap["clientId"] as String
        val campaignId = context.jobDetail.jobDataMap["campaignId"] as String
        val campaignName = context.jobDetail.jobDataMap["campaignName"] as String
        val nextFireTime = context.nextFireTime
        //val keys = scheduler.get(GroupMatcher.groupEquals(JobUtil.getGroupName(clientId,campaignId)))

        if(nextFireTime == null) {
            //FIXME send message for complete of campaign status
        }
        logger.info("Job ** ${context.jobDetail.key.name} ** fired @ ${context.fireTime} for client $clientId with campaign $campaignName : $campaignId")
        Pair(campaignId, clientId)
        jobService.executeJob(Pair(campaignId, clientId))

        logger.info("Next job scheduled @ ${context.nextFireTime}")
    }
}