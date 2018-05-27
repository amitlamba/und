package com.und.scheduler


import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import com.und.job.MessageJobService

@Component
class MessageJob : Job {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var  jobService : MessageJobService

    override fun execute(context: JobExecutionContext) {
        val clientId = context.jobDetail.jobDataMap["clientId"] as String
        val campaignId = context.jobDetail.jobDataMap["campaignId"] as String
        val campaignName = context.jobDetail.jobDataMap["campaignName"] as String

        logger.info("Job ** ${context.jobDetail.key.name} ** fired @ ${context.fireTime} for client $clientId with campaign $campaignName : $campaignId")
        Pair(campaignId, clientId)
        jobService.executeJob(Pair(campaignId, clientId))

        logger.info("Next job scheduled @ ${context.nextFireTime}")
    }
}