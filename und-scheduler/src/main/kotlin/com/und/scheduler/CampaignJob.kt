package com.und.scheduler


import com.und.config.EventStream
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import com.und.job.CampaignJobService
import com.und.model.JobAction
import com.und.model.JobActionStatus
import com.und.model.JobDescriptor
import org.quartz.Scheduler
import org.quartz.impl.matchers.GroupMatcher
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.support.MessageBuilder
import java.util.*

@Component
class CampaignJob : Job {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Autowired
    lateinit var jobService: CampaignJobService

    @Autowired
    protected lateinit var scheduler: Scheduler

    @Autowired
    protected lateinit var eventStream: EventStream

    override fun execute(context: JobExecutionContext) {
        val clientId = context.jobDetail.jobDataMap["clientId"] as String
        val campaignId = context.jobDetail.jobDataMap["campaignId"] as String
        val campaignName = context.jobDetail.jobDataMap["campaignName"] as String
        val nextFireTime = jobGroupNextDate(context.jobDetail.key.group)
        //val keys = scheduler.get(GroupMatcher.groupEquals(JobUtil.getGroupName(clientId,campaignId)))

        if (nextFireTime.isEmpty()) {

            val status = markCompleted(clientId, campaignId, campaignName, JobDescriptor.Action.COMPLETED)
            eventStream.scheduleJobAck().send(MessageBuilder.withPayload(status).build())
        }
        logger.info("Job ** ${context.jobDetail.key.name} ** fired @ ${context.fireTime} for client $clientId with campaign $campaignName : $campaignId")
        Pair(campaignId, clientId)
        jobService.executeJob(Pair(campaignId, clientId))

        logger.info("Next job scheduled @ ${context.nextFireTime}")
    }


    fun jobGroupNextDate(groupName: String): List<Date> {
        val jobKeys = scheduler.getJobKeys(GroupMatcher.groupEquals(groupName))
                .sortedBy { key -> key.name.split("-").last().toLong() }.filterNotNull()
        val jobTriggers = jobKeys.flatMap { key -> scheduler.getTriggersOfJob(key).filterNotNull() }

        return jobTriggers.dropWhile { trigger -> trigger.nextFireTime== null }.map { it.nextFireTime }.sortedByDescending { it }


    }

    @SendTo("scheduleJobAckSend")
    fun markCompleted(clientId: String, campaignId: String, campaignName: String, action: JobDescriptor.Action): JobActionStatus {
        fun jobActionStatus(): JobActionStatus {
            val jobAction = JobAction(
                    campaignId = campaignId,
                    clientId = clientId,
                    campaignName = campaignName,
                    action = action
            )
            val status = JobActionStatus()
            status.jobAction = jobAction
            status.status = JobActionStatus.Status.COMPLETED
            return status
        }

        return jobActionStatus()
    }


}